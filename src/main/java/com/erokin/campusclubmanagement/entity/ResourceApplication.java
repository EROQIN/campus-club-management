package com.erokin.campusclubmanagement.entity;

import com.erokin.campusclubmanagement.enums.ResourceApplicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "resource_applications",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_resource_applicant_time",
                        columnNames = {"resource_id", "applicant_id", "requested_from", "requested_until"}))
public class ResourceApplication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private SharedResource resource;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @Column(nullable = false, length = 400)
    private String purpose;

    private Instant requestedFrom;

    private Instant requestedUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResourceApplicationStatus status = ResourceApplicationStatus.PENDING;

    private Instant respondedAt;

    @Column(length = 300)
    private String replyMessage;
}

