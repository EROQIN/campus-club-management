package com.erokin.campusclubmanagement.service;

import com.erokin.campusclubmanagement.dto.collaboration.CollaborationProposalRequest;
import com.erokin.campusclubmanagement.dto.collaboration.CollaborationProposalResponse;
import com.erokin.campusclubmanagement.dto.collaboration.CollaborationResponseRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CollaborationService {

    CollaborationProposalResponse createProposal(Long clubId, CollaborationProposalRequest request);

    Page<CollaborationProposalResponse> listInitiated(Long clubId, Pageable pageable);

    Page<CollaborationProposalResponse> listAll(Pageable pageable);

    CollaborationProposalResponse respond(Long proposalId, Long clubId, CollaborationResponseRequest request);
}
