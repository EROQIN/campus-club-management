package com.erokin.campusclubmanagement.repository;

import com.erokin.campusclubmanagement.entity.CollaborationProposal;
import com.erokin.campusclubmanagement.entity.CollaborationResponse;
import com.erokin.campusclubmanagement.entity.Club;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollaborationResponseRepository extends JpaRepository<CollaborationResponse, Long> {

    List<CollaborationResponse> findByProposal(CollaborationProposal proposal);

    Optional<CollaborationResponse> findByProposalAndResponderClub(
            CollaborationProposal proposal, Club responderClub);
}
