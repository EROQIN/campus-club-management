package com.erokin.campusclubmanagement.service.impl;

import com.erokin.campusclubmanagement.dto.points.PointLeaderboardEntry;
import com.erokin.campusclubmanagement.dto.points.PointRecordRequest;
import com.erokin.campusclubmanagement.dto.points.PointRecordResponse;
import com.erokin.campusclubmanagement.entity.Club;
import com.erokin.campusclubmanagement.entity.ClubMembership;
import com.erokin.campusclubmanagement.entity.ClubPointRecord;
import com.erokin.campusclubmanagement.entity.User;
import com.erokin.campusclubmanagement.enums.MembershipRole;
import com.erokin.campusclubmanagement.enums.MembershipStatus;
import com.erokin.campusclubmanagement.enums.Role;
import com.erokin.campusclubmanagement.exception.BusinessException;
import com.erokin.campusclubmanagement.exception.ResourceNotFoundException;
import com.erokin.campusclubmanagement.repository.ClubMembershipRepository;
import com.erokin.campusclubmanagement.repository.ClubPointRecordRepository;
import com.erokin.campusclubmanagement.repository.ClubRepository;
import com.erokin.campusclubmanagement.repository.UserRepository;
import com.erokin.campusclubmanagement.service.PointsService;
import com.erokin.campusclubmanagement.util.SecurityUtils;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {

    private final ClubRepository clubRepository;
    private final ClubMembershipRepository membershipRepository;
    private final ClubPointRecordRepository pointRecordRepository;
    private final UserRepository userRepository;

    @Override
    public PointRecordResponse addRecord(Long clubId, PointRecordRequest request) {
        Club club = clubRepository
                .findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("社团不存在"));
        User operator = getCurrentUser();
        ensureManager(operator, club);

        User member = userRepository
                .findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("成员不存在"));

        ClubMembership membership = membershipRepository
                .findByClubAndMember(club, member)
                .orElseThrow(() -> new BusinessException("成员尚未加入该社团"));
        if (membership.getStatus() != MembershipStatus.APPROVED) {
            throw new BusinessException("成员尚未正式加入，无法变更积分");
        }

        if (request.getPoints() == 0) {
            throw new BusinessException("积分变动值必须非零");
        }

        ClubPointRecord record = new ClubPointRecord();
        record.setClub(club);
        record.setMember(member);
        record.setCreatedBy(operator);
        record.setPoints(request.getPoints());
        record.setReason(request.getReason());
        ClubPointRecord saved = pointRecordRepository.save(record);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PointRecordResponse> listRecords(Long clubId, Pageable pageable) {
        Club club = clubRepository
                .findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("社团不存在"));
        ensureManager(getCurrentUser(), club);
        Page<ClubPointRecord> page = pointRecordRepository.findByClub(club, pageable);
        return page.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PointLeaderboardEntry> leaderboard(Long clubId, int limit) {
        Club club = clubRepository
                .findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("社团不存在"));
        ensureManager(getCurrentUser(), club);
        return pointRecordRepository.summarizeByMember(club).stream()
                .map(row -> {
                    User member = (User) row[0];
                    Number total = (Number) row[1];
                    return PointLeaderboardEntry.builder()
                            .memberId(member.getId())
                            .memberName(member.getFullName())
                            .memberEmail(member.getEmail())
                            .totalPoints(total != null ? total.intValue() : 0)
                            .build();
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    private PointRecordResponse toResponse(ClubPointRecord record) {
        return PointRecordResponse.builder()
                .id(record.getId())
                .memberId(record.getMember().getId())
                .memberName(record.getMember().getFullName())
                .memberEmail(record.getMember().getEmail())
                .points(record.getPoints())
                .reason(record.getReason())
                .createdById(record.getCreatedBy() != null ? record.getCreatedBy().getId() : null)
                .createdByName(record.getCreatedBy() != null ? record.getCreatedBy().getFullName() : null)
                .createdAt(record.getCreatedAt())
                .build();
    }

    private User getCurrentUser() {
        Long id = SecurityUtils.getCurrentUserIdOrThrow();
        return userRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private void ensureManager(User user, Club club) {
        if (club.getCreatedBy() != null && club.getCreatedBy().getId().equals(user.getId())) {
            return;
        }
        if (user.getRoles().contains(Role.SYSTEM_ADMIN) || user.getRoles().contains(Role.UNION_STAFF)) {
            return;
        }
        membershipRepository
                .findByClubAndMember(club, user)
                .filter(membership ->
                        membership.getStatus() == MembershipStatus.APPROVED
                                && membership.getMembershipRole() == MembershipRole.LEADER)
                .orElseThrow(() -> new BusinessException("没有权限管理该社团积分"));
    }
}
