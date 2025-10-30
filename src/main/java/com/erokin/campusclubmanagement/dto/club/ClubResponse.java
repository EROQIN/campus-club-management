package com.erokin.campusclubmanagement.dto.club;

import com.erokin.campusclubmanagement.dto.activity.ActivitySummaryResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClubResponse {
    private Long id;
    private String name;
    private String description;
    private String logoUrl;
    private String promoVideoUrl;
    private String category;
    private String contactEmail;
    private String contactPhone;
    private LocalDate foundedDate;
    private Instant createdAt;
    private int memberCount;
    private int pendingApplicants;
    private int activityCountLast30Days;
    private String managerName;
    private List<String> tags;
    private List<ActivitySummaryResponse> recentActivities;
}

