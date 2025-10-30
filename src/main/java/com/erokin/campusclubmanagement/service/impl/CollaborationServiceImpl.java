package com.erokin.campusclubmanagement.service.impl;

import com.erokin.campusclubmanagement.dto.collaboration.CollaborationProposalRequest;
import com.erokin.campusclubmanagement.dto.collaboration.CollaborationProposalResponse;
import com.erokin.campusclubmanagement.dto.collaboration.CollaborationResponseRequest;
import com.erokin.campusclubmanagement.entity.CollaborationProposal;
import com.erokin.campusclubmanagement.entity.CollaborationResponse;
import com.erokin.campusclubmanagement.entity.Club;
import com.erokin.campusclubmanagement.entity.ClubMembership;
import com.erokin.campusclubmanagement.entity.User;
import com.erokin.campusclubmanagement.enums.CollaborationResponseStatus;
import com.erokin.campusclubmanagement.enums.CollaborationStatus;
import com.erokin.campusclubmanagement.enums.MembershipRole;
import com.erokin.campusclubmanagement.enums.MembershipStatus;
import com.erokin.campusclubmanagement.enums.Role;
import com.erokin.campusclubmanagement.exception.BusinessException;
import com.erokin.campusclubmanagement.exception.ResourceNotFoundException;
import com.erokin.campusclubmanagement.repository.CollaborationProposalRepository;
import com.erokin.campusclubmanagement.repository.CollaborationResponseRepository;
import com.erokin.campusclubmanagement.repository.ClubMembershipRepository;
import com.erokin.campusclubmanagement.repository.ClubRepository;
import com.erokin.campusclubmanagement.repository.UserRepository;
import com.erokin.campusclubmanagement.service.CollaborationService;
import com.erokin.campusclubmanagement.util.SecurityUtils;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CollaborationServiceImpl implements CollaborationService {

    private final CollaborationProposalRepository proposalRepository;
    private final CollaborationResponseRepository responseRepository;
    private final ClubRepository clubRepository;
    private final ClubMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    @Override
    public CollaborationProposalResponse createProposal(Long clubId, CollaborationProposalRequest request) {
        Club club = findClub(clubId);
        User operator = getCurrentUser();
        ensureManager(operator, club);

        CollaborationProposal proposal = new CollaborationProposal();
        proposal.setInitiatorClub(club);
        proposal.setInitiator(operator);
        proposal.setTitle(request.getTitle());
        proposal.setDescription(request.getDescription());
        proposal.setCollaborationType(request.getCollaborationType());
        proposal.setRequiredResources(request.getRequiredResources());
        proposal.setStatus(CollaborationStatus.OPEN);

        CollaborationProposal saved = proposalRepository.save(proposal);
        return toResponse(saved, List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollaborationProposalResponse> listInitiated(Long clubId, Pageable pageable) {
        Club club = findClub(clubId);
        ensureManager(getCurrentUser(), club);
        return proposalRepository.findByInitiatorClub(club, pageable)
                .map(this::mapWithResponses);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CollaborationProposalResponse> listAll(Pageable pageable) {
        // Any authenticated user can browse proposals
        getCurrentUser();
        return proposalRepository.findAll(pageable)
                .map(this::mapWithResponses);
    }

    @Override
    public CollaborationProposalResponse respond(
            Long proposalId, Long clubId, CollaborationResponseRequest request) {
        CollaborationProposal proposal = proposalRepository
                .findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("协作提案不存在"));
        Club responderClub = findClub(clubId);
        User operator = getCurrentUser();
        ensureManager(operator, responderClub);

        if (proposal.getInitiatorClub().getId().equals(responderClub.getId())) {
            throw new BusinessException("无法对自己的提案进行回复");
        }

        Optional<CollaborationResponse> existing =
                responseRepository.findByProposalAndResponderClub(proposal, responderClub);

        CollaborationResponse response = existing.orElseGet(CollaborationResponse::new);
        response.setProposal(proposal);
        response.setResponderClub(responderClub);
        response.setResponder(operator);
        response.setMessage(request.getMessage());
        response.setStatus(request.getStatus());
        responseRepository.save(response);

        if (request.getStatus() == CollaborationResponseStatus.ACCEPTED) {
            proposal.setStatus(CollaborationStatus.IN_PROGRESS);
            proposalRepository.save(proposal);
        }

        return mapWithResponses(proposal);
    }

    private CollaborationProposalResponse mapWithResponses(CollaborationProposal proposal) {
        List<CollaborationResponse> responses = responseRepository.findByProposal(proposal);
        return toResponse(proposal, responses);
    }

    private CollaborationProposalResponse toResponse(
            CollaborationProposal proposal, List<CollaborationResponse> responses) {
        return CollaborationProposalResponse.builder()
                .id(proposal.getId())
                .initiatorClubId(proposal.getInitiatorClub().getId())
                .initiatorClubName(proposal.getInitiatorClub().getName())
                .initiatorUserId(proposal.getInitiator().getId())
                .initiatorUserName(proposal.getInitiator().getFullName())
                .title(proposal.getTitle())
                .description(proposal.getDescription())
                .collaborationType(proposal.getCollaborationType())
                .requiredResources(proposal.getRequiredResources())
                .status(proposal.getStatus())
                .createdAt(proposal.getCreatedAt())
                .responses(responses.stream()
                        .map(res -> CollaborationProposalResponse.ProposalResponseDto.builder()
                                .id(res.getId())
                                .responderClubId(res.getResponderClub().getId())
                                .responderClubName(res.getResponderClub().getName())
                                .responderUserId(res.getResponder().getId())
                                .responderUserName(res.getResponder().getFullName())
                                .message(res.getMessage())
                                .status(res.getStatus().name())
                                .createdAt(res.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private Club findClub(Long clubId) {
        return clubRepository
                .findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("社团不存在"));
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
                .orElseThrow(() -> new BusinessException("没有权限进行该操作"));
    }
}
