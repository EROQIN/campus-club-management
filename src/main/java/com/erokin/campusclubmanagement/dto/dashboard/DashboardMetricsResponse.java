package com.erokin.campusclubmanagement.dto.dashboard;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardMetricsResponse {
    private long totalClubs;
    private long newClubsThisSemester;
    private long totalMembers;
    private long activeMembersLast30Days;
    private long totalActivitiesThisMonth;
    private long upcomingActivities;
    private List<TopClubMetric> topActiveClubs;
    private List<ActivityTrendPoint> activityTrend;
    private List<ActivityCategoryMetric> activityCategoryDistribution;

    @Getter
    @Setter
    public static class TopClubMetric {
        private Long clubId;
        private String clubName;
        private long activityCount;
        private long memberCount;
    }

    @Getter
    @Setter
    public static class ActivityTrendPoint {
        private String month;
        private long activityCount;
    }

    @Getter
    @Setter
    public static class ActivityCategoryMetric {
        private String category;
        private long value;
    }
}

