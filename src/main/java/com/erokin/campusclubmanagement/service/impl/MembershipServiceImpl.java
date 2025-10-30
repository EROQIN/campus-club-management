package com.erokin.campusclubmanagement.service.impl;

import com.erokin.campusclubmanagement.dto.membership.MembershipDecisionRequest;
import com.erokin.campusclubmanagement.dto.membership.MembershipRequest;
import com.erokin.campusclubmanagement.dto.membership.MembershipResponse;
import com.erokin.campusclubmanagement.entity.Club;
import com.erokin.campusclubmanagement.entity.ClubMembership;
import com.erokin.campusclubmanagement.entity.Message;
import com.erokin.campusclubmanagement.entity.User;
import com.erokin.campusclubmanagement.enums.MembershipRole;
import com.erokin.campusclubmanagement.enums.MembershipStatus;
import com.erokin.campusclubmanagement.enums.MessageType;
import com.erokin.campusclubmanagement.enums.Role;
import com.erokin.campusclubmanagement.exception.BusinessException;
import com.erokin.campusclubmanagement.exception.ResourceNotFoundException;
import com.erokin.campusclubmanagement.repository.ClubMembershipRepository;
import com.erokin.campusclubmanagement.repository.ClubRepository;
import com.erokin.campusclubmanagement.repository.MessageRepository;
import com.erokin.campusclubmanagement.repository.UserRepository;
import com.erokin.campusclubmanagement.service.MembershipService;
import com.erokin.campusclubmanagement.util.DtoMapper;
import com.erokin.campusclubmanagement.util.SecurityUtils;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MembershipServiceImpl implements MembershipService {

    private final ClubRepository clubRepository;
    private final ClubMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final DtoMapper dtoMapper;

    @Override
    @Transactional
    public MembershipResponse apply(MembershipRequest request) {
        User student = getCurrentUser();
        Club club =
                clubRepository
                        .findById(request.getClubId())
                        .orElseThrow(() -> new ResourceNotFoundException("社团不存在"));

        membershipRepository
                .findByClubAndMember(club, student)
                .ifPresent(existing -> {
                    if (existing.getStatus() == MembershipStatus.PENDING) {
                        throw new BusinessException("已提交申请，请耐心等待");
                    }
                    if (existing.getStatus() == MembershipStatus.APPROVED) {
                        throw new BusinessException("您已是该社团成员");
                    }
                });

        ClubMembership membership = new ClubMembership();
        membership.setClub(club);
        membership.setMember(student);
        membership.setStatus(MembershipStatus.PENDING);
        membership.setMembershipRole(MembershipRole.MEMBER);
        membership.setApplicationReason(request.getApplicationReason());

        ClubMembership saved = membershipRepository.save(membership);
        notifyClubManager(
                club,
                "新的入团申请",
                student.getFullName() + " 提交了加入社团的申请",
                saved.getId());

        return dtoMapper.toMembershipResponse(saved);
    }

    @Override
    @Transactional
    public MembershipResponse decide(Long membershipId, MembershipDecisionRequest request) {
        ClubMembership membership =
                membershipRepository
                        .findById(membershipId)
                        .orElseThrow(() -> new ResourceNotFoundException("申请不存在"));
        User current = getCurrentUser();
        ensureCanManage(current, membership.getClub());

        membership.setStatus(request.getApprove() ? MembershipStatus.APPROVED : MembershipStatus.REJECTED);
        membership.setMembershipRole(
                request.getMembershipRole() != null ? request.getMembershipRole() : MembershipRole.MEMBER);
        membership.setRespondedAt(Instant.now());

        membershipRepository.save(membership);
        notifyUser(
                membership.getMember(),
                request.getApprove() ? "入团申请已通过" : "入团申请未通过",
                (request.getMessage() != null ? request.getMessage() : ""),
                membership.getId());

        return dtoMapper.toMembershipResponse(membership);
    }

    @Override
    @Transactional
    public void withdraw(Long membershipId) {
        ClubMembership membership =
                membershipRepository
                        .findById(membershipId)
                        .orElseThrow(() -> new ResourceNotFoundException("申请不存在"));
        User current = getCurrentUser();
        if (!membership.getMember().getId().equals(current.getId())) {
            throw new BusinessException("只能撤回自己的申请");
        }
        membership.setStatus(MembershipStatus.WITHDRAWN);
        membership.setRespondedAt(Instant.now());
        membershipRepository.save(membership);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipResponse> listMyMemberships() {
        User current = getCurrentUser();
        return membershipRepository.findByMember(current).stream()
                .map(dtoMapper::toMembershipResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipResponse> listClubApplicants(Long clubId) {
        Club club =
                clubRepository
                        .findById(clubId)
                        .orElseThrow(() -> new ResourceNotFoundException("社团不存在"));
        User current = getCurrentUser();
        ensureCanManage(current, club);
        return membershipRepository.findByClubAndStatus(club, MembershipStatus.PENDING).stream()
                .map(dtoMapper::toMembershipResponse)
                .toList();
    }

    private User getCurrentUser() {
        Long id = SecurityUtils.getCurrentUserIdOrThrow();
        return userRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private void ensureCanManage(User user, Club club) {
        if (hasRole(user, Role.SYSTEM_ADMIN) || hasRole(user, Role.UNION_STAFF)) {
            return;
        }
        if (hasRole(user, Role.CLUB_MANAGER)
                && club.getCreatedBy() != null
                && club.getCreatedBy().getId().equals(user.getId())) {
            return;
        }
        throw new BusinessException("没有权限处理该社团申请");
    }

    private void notifyClubManager(Club club, String title, String content, Long membershipId) {
        if (club.getCreatedBy() == null) {
            return;
        }
        Message message = new Message();
        message.setRecipient(club.getCreatedBy());
        message.setType(MessageType.MEMBERSHIP);
        message.setTitle(title);
        message.setContent(content);
        message.setReferenceType("MEMBERSHIP");
        message.setReferenceId(membershipId);
        messageRepository.save(message);
    }

    private void notifyUser(User user, String title, String content, Long membershipId) {
        Message message = new Message();
        message.setRecipient(user);
        message.setType(MessageType.MEMBERSHIP);
        message.setTitle(title);
        message.setContent(content);
        message.setReferenceType("MEMBERSHIP");
        message.setReferenceId(membershipId);
        messageRepository.save(message);
    }

    private boolean hasRole(User user, Role role) {
        return user.getRoles().contains(role);
    }
}

