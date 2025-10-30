package com.erokin.campusclubmanagement.controller;

import com.erokin.campusclubmanagement.dto.activity.ActivityRegistrationRequest;
import com.erokin.campusclubmanagement.dto.activity.ActivityRegistrationResponse;
import com.erokin.campusclubmanagement.dto.activity.ActivityRequest;
import com.erokin.campusclubmanagement.dto.activity.ActivityResponse;
import com.erokin.campusclubmanagement.dto.activity.ActivitySummaryResponse;
import com.erokin.campusclubmanagement.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping("/clubs/{clubId}/activities")
    public ResponseEntity<ActivityResponse> create(
            @PathVariable Long clubId, @Valid @RequestBody ActivityRequest request) {
        return ResponseEntity.ok(activityService.createActivity(clubId, request));
    }

    @PutMapping("/activities/{id}")
    public ResponseEntity<ActivityResponse> update(
            @PathVariable Long id, @Valid @RequestBody ActivityRequest request) {
        return ResponseEntity.ok(activityService.updateActivity(id, request));
    }

    @DeleteMapping("/activities/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/activities/{id}")
    public ResponseEntity<ActivityResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getActivity(id));
    }

    @GetMapping("/clubs/{clubId}/activities")
    public ResponseEntity<Page<ActivitySummaryResponse>> clubActivities(
            @PathVariable Long clubId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 50));
        return ResponseEntity.ok(activityService.listClubActivities(clubId, pageable));
    }

    @GetMapping("/activities/upcoming")
    public ResponseEntity<Page<ActivitySummaryResponse>> upcoming(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 50));
        return ResponseEntity.ok(activityService.listUpcomingActivities(pageable));
    }

    @PostMapping("/activities/{id}/register")
    public ResponseEntity<ActivityRegistrationResponse> register(
            @PathVariable Long id, @Valid @RequestBody ActivityRegistrationRequest request) {
        return ResponseEntity.ok(activityService.registerForActivity(id, request));
    }

    @PostMapping("/registrations/{id}/decision")
    public ResponseEntity<ActivityRegistrationResponse> decide(
            @PathVariable Long id, @RequestParam("approve") boolean approve) {
        return ResponseEntity.ok(activityService.reviewRegistration(id, approve));
    }

    @GetMapping("/activities/{id}/registrations")
    public ResponseEntity<Page<ActivityRegistrationResponse>> registrations(
            @PathVariable Long id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 100));
        return ResponseEntity.ok(activityService.listRegistrationsForActivity(id, pageable));
    }
}

