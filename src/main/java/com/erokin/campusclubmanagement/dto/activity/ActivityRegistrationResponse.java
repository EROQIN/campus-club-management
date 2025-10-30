package com.erokin.campusclubmanagement.dto.activity;

import com.erokin.campusclubmanagement.enums.ActivityRegistrationStatus;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityRegistrationResponse {
    private Long id;
    private Long activityId;
    private String activityTitle;
    private ActivityRegistrationStatus status;
    private String note;
    private Instant createdAt;
}

