package com.erokin.campusclubmanagement.dto.activity;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckInQrResponse {
    private final Long activityId;
    private final String qrUrl;
    private final String token;
    private final Instant expiresAt;
    private final Instant generatedAt;
    private final String payload;
}
