package com.erokin.campusclubmanagement.dto.resource;

import com.erokin.campusclubmanagement.enums.ResourceApplicationStatus;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceApplicationResponse {
    private Long id;
    private Long resourceId;
    private String resourceName;
    private Long applicantId;
    private String applicantName;
    private String purpose;
    private Instant requestedFrom;
    private Instant requestedUntil;
    private ResourceApplicationStatus status;
    private String replyMessage;
    private Instant createdAt;
    private Instant respondedAt;
}

