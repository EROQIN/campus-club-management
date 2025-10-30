package com.erokin.campusclubmanagement.dto.points;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointRecordResponse {
    private final Long id;
    private final Long memberId;
    private final String memberName;
    private final String memberEmail;
    private final int points;
    private final String reason;
    private final Long createdById;
    private final String createdByName;
    private final Instant createdAt;
}
