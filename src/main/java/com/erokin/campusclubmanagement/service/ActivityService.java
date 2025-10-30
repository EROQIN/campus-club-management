package com.erokin.campusclubmanagement.service;

import com.erokin.campusclubmanagement.dto.activity.ActivityRegistrationRequest;
import com.erokin.campusclubmanagement.dto.activity.ActivityRegistrationResponse;
import com.erokin.campusclubmanagement.dto.activity.ActivityRequest;
import com.erokin.campusclubmanagement.dto.activity.ActivityResponse;
import com.erokin.campusclubmanagement.dto.activity.ActivitySummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ActivityService {

    ActivityResponse createActivity(Long clubId, ActivityRequest request);

    ActivityResponse updateActivity(Long activityId, ActivityRequest request);

    void deleteActivity(Long activityId);

    ActivityResponse getActivity(Long activityId);

    Page<ActivitySummaryResponse> listClubActivities(Long clubId, Pageable pageable);

    Page<ActivitySummaryResponse> listUpcomingActivities(Pageable pageable);

    ActivityRegistrationResponse registerForActivity(Long activityId, ActivityRegistrationRequest request);

    ActivityRegistrationResponse reviewRegistration(Long registrationId, boolean approve);

    Page<ActivityRegistrationResponse> listRegistrationsForActivity(Long activityId, Pageable pageable);
}

