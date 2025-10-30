package com.erokin.campusclubmanagement.entity;

import com.erokin.campusclubmanagement.enums.CollaborationStatus;
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
@Table(name = "collaboration_proposals")
public class CollaborationProposal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiator_club_id", nullable = false)
    private Club initiatorClub;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiator_user_id", nullable = false)
    private User initiator;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(length = 100)
    private String collaborationType;

    @Column(length = 300)
    private String requiredResources;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CollaborationStatus status = CollaborationStatus.OPEN;
}
