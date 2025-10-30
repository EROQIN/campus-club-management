package com.erokin.campusclubmanagement.entity;

import com.erokin.campusclubmanagement.enums.ActivityRegistrationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "activity_registrations",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_activity_attendee",
                        columnNames = {"activity_id", "attendee_id"}))
public class ActivityRegistration extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attendee_id", nullable = false)
    private User attendee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityRegistrationStatus status = ActivityRegistrationStatus.PENDING;

    @Column(length = 300)
    private String note;
}

