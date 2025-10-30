package com.erokin.campusclubmanagement.controller;

import com.erokin.campusclubmanagement.dto.points.PointLeaderboardEntry;
import com.erokin.campusclubmanagement.dto.points.PointRecordRequest;
import com.erokin.campusclubmanagement.dto.points.PointRecordResponse;
import com.erokin.campusclubmanagement.service.PointsService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clubs/{clubId}/points")
public class PointsController {

    private final PointsService pointsService;

    public PointsController(PointsService pointsService) {
        this.pointsService = pointsService;
    }

    @PostMapping
    public ResponseEntity<PointRecordResponse> addRecord(
            @PathVariable Long clubId, @Valid @RequestBody PointRecordRequest request) {
        return ResponseEntity.ok(pointsService.addRecord(clubId, request));
    }

    @GetMapping
    public ResponseEntity<Page<PointRecordResponse>> list(
            @PathVariable Long clubId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 100));
        return ResponseEntity.ok(pointsService.listRecords(clubId, pageable));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<PointLeaderboardEntry>> leaderboard(
            @PathVariable Long clubId,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return ResponseEntity.ok(pointsService.leaderboard(clubId, Math.max(limit, 1)));
    }
}
