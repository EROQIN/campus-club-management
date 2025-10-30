package com.erokin.campusclubmanagement.service.impl;

import com.erokin.campusclubmanagement.dto.storage.StoredObjectInfo;
import com.erokin.campusclubmanagement.dto.video.PromoVideoResponse;
import com.erokin.campusclubmanagement.dto.video.SubtitleEditRequest;
import com.erokin.campusclubmanagement.dto.video.SubtitleSegmentResponse;
import com.erokin.campusclubmanagement.dto.video.UpdateSubtitlesRequest;
import com.erokin.campusclubmanagement.entity.Club;
import com.erokin.campusclubmanagement.entity.ClubPromoSubtitle;
import com.erokin.campusclubmanagement.entity.ClubPromoVideo;
import com.erokin.campusclubmanagement.entity.User;
import com.erokin.campusclubmanagement.enums.MembershipRole;
import com.erokin.campusclubmanagement.enums.MembershipStatus;
import com.erokin.campusclubmanagement.enums.PromoVideoStatus;
import com.erokin.campusclubmanagement.enums.Role;
import com.erokin.campusclubmanagement.exception.BusinessException;
import com.erokin.campusclubmanagement.exception.ResourceNotFoundException;
import com.erokin.campusclubmanagement.exception.StorageException;
import com.erokin.campusclubmanagement.repository.ClubMembershipRepository;
import com.erokin.campusclubmanagement.repository.ClubPromoVideoRepository;
import com.erokin.campusclubmanagement.repository.ClubRepository;
import com.erokin.campusclubmanagement.repository.UserRepository;
import com.erokin.campusclubmanagement.service.ClubPromoVideoService;
import com.erokin.campusclubmanagement.service.FileStorageService;
import com.erokin.campusclubmanagement.service.SubtitleGenerationService;
import com.erokin.campusclubmanagement.service.dto.GeneratedSubtitleSegment;
import com.erokin.campusclubmanagement.util.SecurityUtils;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ClubPromoVideoServiceImpl implements ClubPromoVideoService {

    private static final Logger log = LoggerFactory.getLogger(ClubPromoVideoServiceImpl.class);

    private static final long MAX_VIDEO_SIZE_BYTES = 50L * 1024 * 1024;
    private static final int MAX_DURATION_SECONDS = 30;

    private final ClubRepository clubRepository;
    private final ClubPromoVideoRepository promoVideoRepository;
    private final UserRepository userRepository;
    private final ClubMembershipRepository membershipRepository;
    private final FileStorageService fileStorageService;
    private final SubtitleGenerationService subtitleGenerationService;

    @Override
    @Transactional
    public PromoVideoResponse uploadPromoVideo(Long clubId, MultipartFile file, Integer durationSeconds) {
        Club club = findClub(clubId);
        User currentUser = getCurrentUser();
        ensureManager(currentUser, club);

        if (file == null || file.isEmpty()) {
            throw new StorageException("请选择需要上传的视频文件");
        }
        validateVideoFile(file);
        int duration = validateDuration(durationSeconds);

        ClubPromoVideo video = promoVideoRepository.findByClubId(clubId).orElseGet(() -> {
            ClubPromoVideo created = new ClubPromoVideo();
            created.setClub(club);
            return created;
        });

        String previousObjectKey = video.getObjectKey();
        String directory = "videos/club-" + club.getId();

        try (InputStream inputStream = file.getInputStream()) {
            StoredObjectInfo stored = fileStorageService.upload(
                    directory,
                    file.getOriginalFilename(),
                    inputStream,
                    file.getSize(),
                    file.getContentType());

            video.replaceSubtitles(List.of());
            video.setObjectKey(stored.objectKey());
            video.setPlaybackUrl(stored.url());
            video.setOriginalFileName(file.getOriginalFilename());
            video.setFileSizeBytes(file.getSize());
            video.setDurationSeconds(duration);
            video.setStatus(PromoVideoStatus.UPLOADED);
            video.setTranscriptionTaskId(null);
            video.setSubtitlesUpdatedAt(null);

            ClubPromoVideo saved = promoVideoRepository.save(video);
            club.setPromoVideoUrl(saved.getPlaybackUrl());
            clubRepository.save(club);

            deletePreviousObject(previousObjectKey);
            return toResponse(saved);
        } catch (IOException e) {
            throw new StorageException("上传视频文件失败，请稍后再试。", e);
        }
    }

    @Override
    @Transactional
    public PromoVideoResponse generateSubtitles(Long clubId) {
        Club club = findClub(clubId);
        User currentUser = getCurrentUser();
        ensureManager(currentUser, club);

        ClubPromoVideo video =
                promoVideoRepository
                        .findByClubId(clubId)
                        .orElseThrow(() -> new BusinessException("尚未上传宣传视频，无法生成字幕。"));

        if (video.getStatus() == PromoVideoStatus.TRANSCRIBING) {
            throw new BusinessException("字幕已在生成中，请稍候刷新。");
        }

        video.setStatus(PromoVideoStatus.TRANSCRIBING);
        promoVideoRepository.saveAndFlush(video);

        try {
            List<GeneratedSubtitleSegment> generated =
                    subtitleGenerationService.generateSubtitles(video.getPlaybackUrl());

            AtomicInteger sequence = new AtomicInteger(1);
            List<ClubPromoSubtitle> subtitles =
                    generated.stream()
                            .map(segment -> {
                                ClubPromoSubtitle subtitle = new ClubPromoSubtitle();
                                subtitle.setSequence(sequence.getAndIncrement());
                                subtitle.setStartMs(Math.max(segment.startMs(), 0));
                                subtitle.setEndMs(Math.max(segment.endMs(), subtitle.getStartMs() + 500));
                                subtitle.setText(segment.text());
                                subtitle.setAutoGenerated(true);
                                return subtitle;
                            })
                            .collect(Collectors.toList());

            video.replaceSubtitles(subtitles);
            video.setStatus(PromoVideoStatus.READY);
            video.setSubtitlesUpdatedAt(Instant.now());
            ClubPromoVideo saved = promoVideoRepository.save(video);
            return toResponse(saved);
        } catch (RuntimeException ex) {
            log.error("Failed to generate subtitles for club {}: {}", clubId, ex.getMessage(), ex);
            video.setStatus(PromoVideoStatus.FAILED);
            promoVideoRepository.save(video);
            throw ex;
        }
    }

    @Override
    @Transactional
    public PromoVideoResponse updateSubtitles(Long clubId, UpdateSubtitlesRequest request) {
        Club club = findClub(clubId);
        User currentUser = getCurrentUser();
        ensureManager(currentUser, club);

        ClubPromoVideo video =
                promoVideoRepository
                        .findByClubId(clubId)
                        .orElseThrow(() -> new BusinessException("尚未上传宣传视频，无法保存字幕。"));

        List<SubtitleEditRequest> items =
                Optional.ofNullable(request.getSubtitles()).orElse(List.of());

        List<ClubPromoSubtitle> subtitles =
                items.stream()
                        .sorted(Comparator.comparing(SubtitleEditRequest::getSequence))
                        .map(item -> {
                            validateSubtitle(item);
                            ClubPromoSubtitle subtitle = new ClubPromoSubtitle();
                            subtitle.setSequence(item.getSequence());
                            subtitle.setStartMs(item.getStartMs());
                            subtitle.setEndMs(item.getEndMs());
                            subtitle.setText(item.getText().trim());
                            subtitle.setAutoGenerated(item.isAutoGenerated());
                            return subtitle;
                        })
                        .collect(Collectors.toList());

        // Ensure sequences are continuous starting from 1
        AtomicInteger sequence = new AtomicInteger(1);
        subtitles.forEach(subtitle -> subtitle.setSequence(sequence.getAndIncrement()));

        video.replaceSubtitles(subtitles);
        video.setStatus(PromoVideoStatus.READY);
        video.setSubtitlesUpdatedAt(Instant.now());
        ClubPromoVideo saved = promoVideoRepository.save(video);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PromoVideoResponse getPromoVideo(Long clubId) {
        ClubPromoVideo video =
                promoVideoRepository
                        .findByClubId(clubId)
                        .orElseThrow(() -> new ResourceNotFoundException("该社团尚未上传宣传视频"));
        return toResponse(video);
    }

    private void validateVideoFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (file.getSize() > MAX_VIDEO_SIZE_BYTES) {
            throw new StorageException("视频大小不能超过 50MB");
        }
        if (contentType == null || !contentType.equalsIgnoreCase("video/mp4")) {
            throw new StorageException("仅支持上传 mp4 格式视频");
        }
    }

    private int validateDuration(Integer durationSeconds) {
        if (durationSeconds == null || durationSeconds <= 0) {
            throw new BusinessException("请提供视频时长（秒）用于校验");
        }
        if (durationSeconds > MAX_DURATION_SECONDS) {
            throw new BusinessException("视频时长不能超过 30 秒");
        }
        return durationSeconds;
    }

    private void validateSubtitle(SubtitleEditRequest item) {
        if (item.getStartMs() >= item.getEndMs()) {
            throw new BusinessException("字幕的开始时间必须早于结束时间");
        }
        if (!StringUtils.hasText(item.getText())) {
            throw new BusinessException("字幕内容不能为空");
        }
        if (item.getText().length() > 500) {
            throw new BusinessException("字幕内容不能超过 500 字符");
        }
    }

    private void deletePreviousObject(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return;
        }
        try {
            fileStorageService.delete(objectKey);
        } catch (RuntimeException ex) {
            log.warn("Failed to delete previous video object '{}': {}", objectKey, ex.getMessage());
        }
    }

    private Club findClub(Long id) {
        return clubRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("社团不存在"));
    }

    private User getCurrentUser() {
        Long id = SecurityUtils.getCurrentUserIdOrThrow();
        return userRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private void ensureManager(User user, Club club) {
        if (Objects.equals(club.getCreatedBy() != null ? club.getCreatedBy().getId() : null, user.getId())) {
            return;
        }
        if (user.getRoles().contains(Role.SYSTEM_ADMIN) || user.getRoles().contains(Role.UNION_STAFF)) {
            return;
        }
        boolean isLeader = membershipRepository
                .findByClubAndMember(club, user)
                .filter(membership ->
                        membership.getStatus() == MembershipStatus.APPROVED
                                && membership.getMembershipRole() == MembershipRole.LEADER)
                .isPresent();
        if (!isLeader) {
            throw new BusinessException("没有权限管理该社团视频");
        }
    }

    private PromoVideoResponse toResponse(ClubPromoVideo video) {
        PromoVideoResponse response = new PromoVideoResponse();
        response.setId(video.getId());
        response.setClubId(video.getClub().getId());
        response.setPlaybackUrl(video.getPlaybackUrl());
        response.setOriginalFileName(video.getOriginalFileName());
        response.setStatus(video.getStatus());
        response.setDurationSeconds(video.getDurationSeconds());
        response.setFileSizeBytes(video.getFileSizeBytes());
        response.setCreatedAt(video.getCreatedAt());
        response.setUpdatedAt(video.getUpdatedAt());
        response.setSubtitlesUpdatedAt(video.getSubtitlesUpdatedAt());
        List<SubtitleSegmentResponse> subtitles =
                video.getSubtitles().stream()
                        .sorted(Comparator.comparing(ClubPromoSubtitle::getSequence))
                        .map(subtitle -> {
                            SubtitleSegmentResponse segment = new SubtitleSegmentResponse();
                            segment.setId(subtitle.getId());
                            segment.setSequence(subtitle.getSequence());
                            segment.setStartMs(subtitle.getStartMs());
                            segment.setEndMs(subtitle.getEndMs());
                            segment.setText(subtitle.getText());
                            segment.setAutoGenerated(subtitle.isAutoGenerated());
                            return segment;
                        })
                        .toList();
        response.setSubtitles(subtitles);
        return response;
    }
}
