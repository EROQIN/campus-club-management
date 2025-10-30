package com.erokin.campusclubmanagement.controller;

import com.erokin.campusclubmanagement.dto.resource.ResourceApplicationDecisionRequest;
import com.erokin.campusclubmanagement.dto.resource.ResourceApplicationRequest;
import com.erokin.campusclubmanagement.dto.resource.ResourceApplicationResponse;
import com.erokin.campusclubmanagement.service.ResourceApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceApplicationController {

    private final ResourceApplicationService resourceApplicationService;

    @PostMapping("/{resourceId}/applications")
    public ResponseEntity<ResourceApplicationResponse> apply(
            @PathVariable Long resourceId, @Valid @RequestBody ResourceApplicationRequest request) {
        return ResponseEntity.ok(resourceApplicationService.apply(resourceId, request));
    }

    @GetMapping("/applications/my")
    public ResponseEntity<List<ResourceApplicationResponse>> myApplications() {
        return ResponseEntity.ok(resourceApplicationService.listMyApplications());
    }

    @GetMapping("/{resourceId}/applications")
    @PreAuthorize("hasRole('CLUB_MANAGER') or hasRole('UNION_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<List<ResourceApplicationResponse>> resourceApplications(
            @PathVariable Long resourceId) {
        return ResponseEntity.ok(resourceApplicationService.listApplicationsForResource(resourceId));
    }

    @PostMapping("/applications/{applicationId}/decision")
    @PreAuthorize("hasRole('CLUB_MANAGER') or hasRole('UNION_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ResourceApplicationResponse> decide(
            @PathVariable Long applicationId,
            @Valid @RequestBody ResourceApplicationDecisionRequest request) {
        return ResponseEntity.ok(resourceApplicationService.decide(applicationId, request));
    }

    @DeleteMapping("/applications/{applicationId}")
    public ResponseEntity<Void> cancel(@PathVariable Long applicationId) {
        resourceApplicationService.cancel(applicationId);
        return ResponseEntity.noContent().build();
    }
}

