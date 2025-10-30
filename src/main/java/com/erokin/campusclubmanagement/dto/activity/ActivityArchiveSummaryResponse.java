package com.erokin.campusclubmanagement.dto.activity;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActivityArchiveSummaryResponse {
    private final Long id;
    private final Long activityId;
    private final String activityTitle;
    private final Instant archivedAt;
    private final List<String> photoUrls;
    private final String createdByName;
}
