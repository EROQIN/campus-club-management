package com.erokin.campusclubmanagement.dto.activity;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActivityArchiveResponse {
    private final Long id;
    private final Long activityId;
    private final String activityTitle;
    private final String summary;
    private final List<String> photoUrls;
    private final Instant archivedAt;
    private final String createdByName;
    private final String shareUrl;
}
