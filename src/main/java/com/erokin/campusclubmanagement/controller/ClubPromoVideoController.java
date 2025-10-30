package com.erokin.campusclubmanagement.controller;

import com.erokin.campusclubmanagement.dto.video.PromoVideoResponse;
import com.erokin.campusclubmanagement.dto.video.UpdateSubtitlesRequest;
import com.erokin.campusclubmanagement.service.ClubPromoVideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/clubs/{clubId}/promo-video")
@RequiredArgsConstructor
public class ClubPromoVideoController {

    private final ClubPromoVideoService promoVideoService;

    @GetMapping
    public ResponseEntity<PromoVideoResponse> get(@PathVariable Long clubId) {
        return ResponseEntity.ok(promoVideoService.getPromoVideo(clubId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CLUB_MANAGER') or hasRole('UNION_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<PromoVideoResponse> upload(
            @PathVariable Long clubId,
            @RequestPart("file") MultipartFile file,
            @RequestParam("durationSeconds") Integer durationSeconds) {
        return ResponseEntity.ok(promoVideoService.uploadPromoVideo(clubId, file, durationSeconds));
    }

    @PostMapping("/subtitles/ai")
    @PreAuthorize("hasRole('CLUB_MANAGER') or hasRole('UNION_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<PromoVideoResponse> generateSubtitles(@PathVariable Long clubId) {
        return ResponseEntity.ok(promoVideoService.generateSubtitles(clubId));
    }

    @PutMapping("/subtitles")
    @PreAuthorize("hasRole('CLUB_MANAGER') or hasRole('UNION_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<PromoVideoResponse> updateSubtitles(
            @PathVariable Long clubId, @Valid @RequestBody UpdateSubtitlesRequest request) {
        return ResponseEntity.ok(promoVideoService.updateSubtitles(clubId, request));
    }
}
