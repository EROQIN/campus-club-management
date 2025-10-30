package com.erokin.campusclubmanagement.dto.activity;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityCheckInResponse {
    private Long id;
    private Long activityId;
    private Long attendeeId;
    private String attendeeName;
    private Instant checkedInAt;
    private String method;
}
