package com.erokin.campusclubmanagement.dto.points;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointLeaderboardEntry {
    private final Long memberId;
    private final String memberName;
    private final String memberEmail;
    private final int totalPoints;
}
