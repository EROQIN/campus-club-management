package com.erokin.campusclubmanagement.service.impl;

import com.erokin.campusclubmanagement.dto.club.ClubRequest;
import com.erokin.campusclubmanagement.dto.club.ClubResponse;
import com.erokin.campusclubmanagement.dto.club.ClubSummaryResponse;
import com.erokin.campusclubmanagement.entity.Club;
import com.erokin.campusclubmanagement.entity.InterestTag;
import com.erokin.campusclubmanagement.entity.User;
import com.erokin.campusclubmanagement.enums.MembershipStatus;
import com.erokin.campusclubmanagement.enums.Role;
import com.erokin.campusclubmanagement.exception.BusinessException;
import com.erokin.campusclubmanagement.exception.ResourceNotFoundException;
import com.erokin.campusclubmanagement.repository.ActivityRepository;
import com.erokin.campusclubmanagement.repository.ClubMembershipRepository;
import com.erokin.campusclubmanagement.repository.ClubRepository;
import com.erokin.campusclubmanagement.repository.InterestTagRepository;
import com.erokin.campusclubmanagement.repository.UserRepository;
import com.erokin.campusclubmanagement.service.ClubService;
import com.erokin.campusclubmanagement.util.DtoMapper;
import com.erokin.campusclubmanagement.util.SecurityUtils;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ClubServiceImpl implements ClubService {

    private final ClubRepository clubRepository;
    private final InterestTagRepository interestTagRepository;
    private final UserRepository userRepository;
    private final ClubMembershipRepository clubMembershipRepository;
    private final ActivityRepository activityRepository;
    private final DtoMapper dtoMapper;

    @Override
    @Transactional
    public ClubResponse createClub(ClubRequest request) {
        User currentUser = getCurrentUser();
        ensureManagerPrivilege(currentUser);
        clubRepository
                .findByNameIgnoreCase(request.getName())
                .ifPresent(club -> {
                    throw new BusinessException("社团已存在");
                });

        Club club = new Club();
        applyRequest(club, request);
        club.setCreatedBy(currentUser);
        club.setTags(resolveTags(request.getTags()));

        Club saved = clubRepository.save(club);
        return buildClubResponse(saved);
    }

    @Override
    @Transactional
    public ClubResponse updateClub(Long clubId, ClubRequest request) {
        Club club =
                clubRepository
                        .findById(clubId)
                        .orElseThrow(() -> new ResourceNotFoundException("未找到社团"));
        User currentUser = getCurrentUser();
        ensureCanEditClub(currentUser, club);

        applyRequest(club, request);
        club.setTags(resolveTags(request.getTags()));

        return buildClubResponse(clubRepository.save(club));
    }

    @Override
    @Transactional
    public void deleteClub(Long clubId) {
        Club club =
                clubRepository
                        .findById(clubId)
                        .orElseThrow(() -> new ResourceNotFoundException("未找到社团"));
        User currentUser = getCurrentUser();
        if (!hasRole(currentUser, Role.SYSTEM_ADMIN)) {
            throw new BusinessException("仅系统管理员可以删除社团");
        }
        clubRepository.delete(club);
    }

    @Override
    @Transactional(readOnly = true)
    public ClubResponse getClub(Long clubId) {
        Club club =
                clubRepository
                        .findById(clubId)
                        .orElseThrow(() -> new ResourceNotFoundException("未找到社团"));
        return buildClubResponse(club);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClubSummaryResponse> searchClubs(String keywords, String category, Pageable pageable) {
        Page<Club> clubs = clubRepository.search(trimToNull(keywords), trimToNull(category), pageable);
        return clubs.map(
                club ->
                        dtoMapper.toClubSummary(
                                club,
                                (int)
                                        clubMembershipRepository.countByClubAndStatus(
                                                club, MembershipStatus.APPROVED),
                                (int) activityRepository.countByClubAndStartTimeAfter(
                                        club, Instant.now().minus(90, ChronoUnit.DAYS))));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClubSummaryResponse> recommendClubs(Pageable pageable) {
        User current = getCurrentUser();
        Set<Long> joinedClubIds =
                clubMembershipRepository.findByMember(current).stream()
                        .filter(membership -> membership.getStatus() == MembershipStatus.APPROVED)
                        .map(membership -> membership.getClub().getId())
                        .collect(Collectors.toSet());

        Set<String> interestNames =
                current.getInterests().stream().map(InterestTag::getName).collect(Collectors.toSet());

        List<Club> clubs = clubRepository.findAll();
        List<ClubSummaryResponse> scored =
                clubs.stream()
                        .filter(club -> !joinedClubIds.contains(club.getId()))
                        .map(
                                club -> {
                                    int memberCount =
                                            (int)
                                                    clubMembershipRepository.countByClubAndStatus(
                                                            club, MembershipStatus.APPROVED);
                                    int activityCount =
                                            (int)
                                                    activityRepository.countByClubAndStartTimeAfter(
                                                            club, Instant.now().minus(90, ChronoUnit.DAYS));
                                    ClubSummaryResponse summary =
                                            dtoMapper.toClubSummary(club, memberCount, activityCount);

                                    int matchScore =
                                            (int)
                                                    club.getTags().stream()
                                                            .map(InterestTag::getName)
                                                            .filter(interestNames::contains)
                                                            .count();
                                    if (club.getCategory() != null
                                            && interestNames.stream()
                                                    .anyMatch(
                                                            name ->
                                                                    club.getCategory()
                                                                            .toLowerCase()
                                                                            .contains(name.toLowerCase()))) {
                                        matchScore += 1;
                                    }
                                    summary.setRecommendationScore(matchScore);
                                    return summary;
                                })
                        .sorted((a, b) -> Integer.compare(b.getRecommendationScore(), a.getRecommendationScore()))
                        .toList();

        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        int fromIndex = Math.min(page * size, scored.size());
        int toIndex = Math.min(fromIndex + size, scored.size());
        List<ClubSummaryResponse> content = scored.subList(fromIndex, toIndex);
        return new PageImpl<>(content, PageRequest.of(page, size), scored.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubSummaryResponse> listMyClubs() {
        User current = getCurrentUser();
        return clubRepository.findAll().stream()
                .filter(
                        club ->
                                club.getCreatedBy() != null
                                        && club.getCreatedBy().getId().equals(current.getId()))
                .map(
                        club ->
                                dtoMapper.toClubSummary(
                                        club,
                                        (int)
                                                clubMembershipRepository.countByClubAndStatus(
                                                        club, MembershipStatus.APPROVED),
                                        (int)
                                                activityRepository.countByClubAndStartTimeAfter(
                                                        club, Instant.now().minus(90, ChronoUnit.DAYS))))
                .toList();
    }

    private ClubResponse buildClubResponse(Club club) {
        int memberCount =
                (int)
                        clubMembershipRepository.countByClubAndStatus(
                                club, MembershipStatus.APPROVED);
        int pendingCount =
                (int)
                        clubMembershipRepository.countByClubAndStatus(
                                club, MembershipStatus.PENDING);
        int activityCount =
                (int)
                        activityRepository.countByClubAndStartTimeAfter(
                                club, Instant.now().minus(30, ChronoUnit.DAYS));

        ClubResponse response = dtoMapper.toClubResponse(club, memberCount, pendingCount, activityCount);
        response.setRecentActivities(
                activityRepository.findTop5ByClubOrderByStartTimeDesc(club).stream()
                        .map(activity -> dtoMapper.toActivitySummary(activity, 0))
                        .toList());
        return response;
    }

    private void applyRequest(Club club, ClubRequest request) {
        club.setName(request.getName());
        club.setDescription(request.getDescription());
        club.setLogoUrl(request.getLogoUrl());
        club.setPromoVideoUrl(request.getPromoVideoUrl());
        club.setCategory(request.getCategory());
        club.setContactEmail(request.getContactEmail());
        club.setContactPhone(request.getContactPhone());
        club.setFoundedDate(request.getFoundedDate());
    }

    private Set<InterestTag> resolveTags(List<String> tagNames) {
        Set<InterestTag> tags = new HashSet<>();
        if (tagNames == null) {
            return tags;
        }
        for (String name : tagNames) {
            if (!StringUtils.hasText(name)) {
                continue;
            }
            Optional<InterestTag> existing = interestTagRepository.findByNameIgnoreCase(name.trim());
            InterestTag tag = existing.orElseGet(() -> {
                InterestTag created = new InterestTag();
                created.setName(name.trim());
                return interestTagRepository.save(created);
            });
            tags.add(tag);
        }
        return tags;
    }

    private User getCurrentUser() {
        Long id = SecurityUtils.getCurrentUserIdOrThrow();
        return userRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private void ensureManagerPrivilege(User user) {
        if (!(hasRole(user, Role.CLUB_MANAGER)
                || hasRole(user, Role.SYSTEM_ADMIN)
                || hasRole(user, Role.UNION_STAFF))) {
            throw new BusinessException("需要社团负责人或管理员权限");
        }
    }

    private void ensureCanEditClub(User user, Club club) {
        if (hasRole(user, Role.SYSTEM_ADMIN) || hasRole(user, Role.UNION_STAFF)) {
            return;
        }
        if (hasRole(user, Role.CLUB_MANAGER) && club.getCreatedBy() != null && club.getCreatedBy().getId().equals(user.getId())) {
            return;
        }
        throw new BusinessException("没有权限编辑该社团");
    }

    private boolean hasRole(User user, Role role) {
        return user.getRoles().contains(role);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
