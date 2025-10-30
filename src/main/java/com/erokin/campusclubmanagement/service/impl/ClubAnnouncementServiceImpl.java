package com.erokin.campusclubmanagement.service.impl;

import com.erokin.campusclubmanagement.dto.announcement.AnnouncementRequest;
import com.erokin.campusclubmanagement.dto.announcement.AnnouncementResponse;
import com.erokin.campusclubmanagement.entity.Club;
import com.erokin.campusclubmanagement.entity.ClubAnnouncement;
import com.erokin.campusclubmanagement.entity.User;
import com.erokin.campusclubmanagement.enums.Role;
import com.erokin.campusclubmanagement.exception.BusinessException;
import com.erokin.campusclubmanagement.exception.ResourceNotFoundException;
import com.erokin.campusclubmanagement.repository.ClubAnnouncementRepository;
import com.erokin.campusclubmanagement.repository.ClubRepository;
import com.erokin.campusclubmanagement.repository.UserRepository;
import com.erokin.campusclubmanagement.service.ClubAnnouncementService;
import com.erokin.campusclubmanagement.util.DtoMapper;
import com.erokin.campusclubmanagement.util.SecurityUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ClubAnnouncementServiceImpl implements ClubAnnouncementService {

    private final ClubRepository clubRepository;
    private final ClubAnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final DtoMapper dtoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AnnouncementResponse> listAnnouncements(Long clubId) {
        Club club =
                clubRepository
                        .findById(clubId)
                        .orElseThrow(() -> new ResourceNotFoundException("社团不存在"));
        return announcementRepository.findByClubOrderByCreatedAtDesc(club).stream()
                .map(dtoMapper::toAnnouncementResponse)
                .toList();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('CLUB_MANAGER') or hasRole('UNION_STAFF') or hasRole('SYSTEM_ADMIN')")
    public AnnouncementResponse publish(Long clubId, AnnouncementRequest request) {
        if (!StringUtils.hasText(request.getTitle()) || !StringUtils.hasText(request.getContent())) {
            throw new BusinessException("标题和内容不能为空");
        }
        Club club =
                clubRepository
                        .findById(clubId)
                        .orElseThrow(() -> new ResourceNotFoundException("社团不存在"));
        User author = getCurrentUser();
        ensureManager(author, club);

        ClubAnnouncement announcement = new ClubAnnouncement();
        announcement.setClub(club);
        announcement.setAuthor(author);
        announcement.setTitle(request.getTitle().trim());
        announcement.setContent(request.getContent().trim());

        return dtoMapper.toAnnouncementResponse(announcementRepository.save(announcement));
    }

    private User getCurrentUser() {
        Long id = SecurityUtils.getCurrentUserIdOrThrow();
        return userRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private void ensureManager(User user, Club club) {
        if (user.getRoles().contains(Role.SYSTEM_ADMIN) || user.getRoles().contains(Role.UNION_STAFF)) {
            return;
        }
        if (club.getCreatedBy() != null && club.getCreatedBy().getId().equals(user.getId())) {
            return;
        }
        throw new BusinessException("没有权限管理该社团");
    }
}

