package com.erokin.campusclubmanagement.repository;

import com.erokin.campusclubmanagement.entity.CollaborationProposal;
import com.erokin.campusclubmanagement.entity.Club;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollaborationProposalRepository extends JpaRepository<CollaborationProposal, Long> {

    Page<CollaborationProposal> findByInitiatorClub(Club club, Pageable pageable);
}
