package com.erokin.campusclubmanagement.entity;

import com.erokin.campusclubmanagement.enums.CollaborationResponseStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "collaboration_responses")
public class CollaborationResponse extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proposal_id", nullable = false)
    private CollaborationProposal proposal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responder_club_id", nullable = false)
    private Club responderClub;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responder_user_id", nullable = false)
    private User responder;

    @Column(nullable = false, length = 600)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CollaborationResponseStatus status = CollaborationResponseStatus.PENDING;
}
