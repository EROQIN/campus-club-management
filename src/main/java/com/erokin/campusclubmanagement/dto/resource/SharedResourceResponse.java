package com.erokin.campusclubmanagement.dto.resource;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SharedResourceResponse {
    private Long id;
    private Long clubId;
    private String clubName;
    private String name;
    private String resourceType;
    private String description;
    private Instant availableFrom;
    private Instant availableUntil;
    private String contact;
}

