package com.erokin.campusclubmanagement.dto.activity;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivitySummaryResponse {
    private Long id;
    private String title;
    private String clubName;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private String location;
    private int attendeeCount;
    private Integer capacity;
    private boolean requiresApproval;
}
