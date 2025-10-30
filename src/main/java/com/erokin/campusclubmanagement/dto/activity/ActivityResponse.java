package com.erokin.campusclubmanagement.dto.activity;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityResponse {
    private Long id;
    private Long clubId;
    private String clubName;
    private String title;
    private String description;
    private String location;
    private Instant startTime;
    private Instant endTime;
    private Integer capacity;
    private String bannerUrl;
    private boolean requiresApproval;
    private int attendeeCount;
}

