package com.erokin.campusclubmanagement.controller;

import com.erokin.campusclubmanagement.dto.announcement.AnnouncementRequest;
import com.erokin.campusclubmanagement.dto.announcement.AnnouncementResponse;
import com.erokin.campusclubmanagement.service.ClubAnnouncementService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clubs/{clubId}/messages")
@RequiredArgsConstructor
public class ClubAnnouncementController {

    private final ClubAnnouncementService clubAnnouncementService;

    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> list(@PathVariable Long clubId) {
        return ResponseEntity.ok(clubAnnouncementService.listAnnouncements(clubId));
    }

    @PostMapping
    @PreAuthorize("hasRole('CLUB_MANAGER') or hasRole('UNION_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<AnnouncementResponse> publish(
            @PathVariable Long clubId, @Valid @RequestBody AnnouncementRequest request) {
        return ResponseEntity.ok(clubAnnouncementService.publish(clubId, request));
    }
}

