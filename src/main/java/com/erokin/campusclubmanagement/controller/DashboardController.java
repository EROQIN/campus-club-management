package com.erokin.campusclubmanagement.controller;

import com.erokin.campusclubmanagement.dto.dashboard.DashboardMetricsResponse;
import com.erokin.campusclubmanagement.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public ResponseEntity<DashboardMetricsResponse> overview() {
        return ResponseEntity.ok(dashboardService.getMetrics());
    }
}

