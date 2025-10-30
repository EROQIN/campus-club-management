package com.erokin.campusclubmanagement.service.impl;

import com.erokin.campusclubmanagement.dto.dashboard.DashboardMetricsResponse;
import com.erokin.campusclubmanagement.entity.Activity;
import com.erokin.campusclubmanagement.entity.Club;
import com.erokin.campusclubmanagement.enums.ActivityRegistrationStatus;
import com.erokin.campusclubmanagement.enums.MembershipStatus;
import com.erokin.campusclubmanagement.repository.ActivityRegistrationRepository;
import com.erokin.campusclubmanagement.repository.ActivityRepository;
import com.erokin.campusclubmanagement.repository.ClubMembershipRepository;
import com.erokin.campusclubmanagement.repository.ClubRepository;
import com.erokin.campusclubmanagement.service.DashboardService;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final ClubRepository clubRepository;
    private final ClubMembershipRepository membershipRepository;
    private final ActivityRepository activityRepository;
    private final ActivityRegistrationRepository registrationRepository;

    @Override
    public DashboardMetricsResponse getMetrics() {
        DashboardMetricsResponse response = new DashboardMetricsResponse();
        Instant now = Instant.now();
        Instant semesterStart = now.minus(120, ChronoUnit.DAYS);
        Instant last30Days = now.minus(30, ChronoUnit.DAYS);

        List<Club> clubs = clubRepository.findAll();
        response.setTotalClubs(clubs.size());
        response.setNewClubsThisSemester(
                clubs.stream().filter(club -> club.getCreatedAt().isAfter(semesterStart)).count());
        response.setTotalMembers(membershipRepository.countByStatus(MembershipStatus.APPROVED));
        response.setActiveMembersLast30Days(
                registrationRepository.countByStatusAndCreatedAtAfter(
                        ActivityRegistrationStatus.APPROVED, last30Days));
        response.setTotalActivitiesThisMonth(
                activityRepository.countByStartTimeAfter(last30Days));
        response.setUpcomingActivities(activityRepository.countByStartTimeAfter(now));

        response.setTopActiveClubs(buildTopClubs(clubs, last30Days));
        response.setActivityTrend(buildActivityTrend(last30Days));
        response.setActivityCategoryDistribution(buildCategoryDistribution(clubs));
        return response;
    }

    private List<DashboardMetricsResponse.TopClubMetric> buildTopClubs(
            List<Club> clubs, Instant last30Days) {
        return clubs.stream()
                .map(
                        club -> {
                            long activityCount =
                                    activityRepository.countByClubAndStartTimeAfter(
                                            club, last30Days);
                            long memberCount =
                                    membershipRepository.countByClubAndStatus(
                                            club, MembershipStatus.APPROVED);
                            DashboardMetricsResponse.TopClubMetric metric =
                                    new DashboardMetricsResponse.TopClubMetric();
                            metric.setClubId(club.getId());
                            metric.setClubName(club.getName());
                            metric.setActivityCount(activityCount);
                            metric.setMemberCount(memberCount);
                            return metric;
                        })
                .sorted(Comparator.comparingLong(DashboardMetricsResponse.TopClubMetric::getActivityCount).reversed())
                .limit(5)
                .toList();
    }

    private List<DashboardMetricsResponse.ActivityTrendPoint> buildActivityTrend(Instant since) {
        List<Activity> activities =
                activityRepository.findByStartTimeAfter(since, Pageable.unpaged()).getContent();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM")
                .withZone(ZoneId.systemDefault());
        Map<String, Long> grouped =
                activities.stream()
                        .collect(
                                Collectors.groupingBy(
                                        activity -> formatter.format(activity.getStartTime()),
                                        Collectors.counting()));
        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(
                        entry -> {
                            DashboardMetricsResponse.ActivityTrendPoint point =
                                    new DashboardMetricsResponse.ActivityTrendPoint();
                            point.setMonth(entry.getKey());
                            point.setActivityCount(entry.getValue());
                            return point;
                        })
                .toList();
    }

    private List<DashboardMetricsResponse.ActivityCategoryMetric> buildCategoryDistribution(
            List<Club> clubs) {
        Map<String, Long> grouped =
                clubs.stream()
                        .filter(club -> club.getCategory() != null)
                        .collect(
                                Collectors.groupingBy(
                                        Club::getCategory, Collectors.counting()));
        return grouped.entrySet().stream()
                .map(
                        entry -> {
                            DashboardMetricsResponse.ActivityCategoryMetric metric =
                                    new DashboardMetricsResponse.ActivityCategoryMetric();
                            metric.setCategory(entry.getKey());
                            metric.setValue(entry.getValue());
                            return metric;
                        })
                .toList();
    }
}

