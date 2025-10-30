package com.erokin.campusclubmanagement.entity;

import com.erokin.campusclubmanagement.enums.CheckInMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "activity_check_ins")
public class ActivityCheckIn extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attendee_id", nullable = false)
    private User attendee;

    @Column(nullable = false)
    private Instant checkedInAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(length = 40, nullable = false)
    private CheckInMethod method = CheckInMethod.QR_CODE;
}
