package com.erokin.campusclubmanagement.controller;

import com.erokin.campusclubmanagement.dto.club.ClubRequest;
import com.erokin.campusclubmanagement.dto.club.ClubResponse;
import com.erokin.campusclubmanagement.dto.club.ClubSummaryResponse;
import com.erokin.campusclubmanagement.service.ClubService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/clubs")
public class ClubController {

    private final ClubService clubService;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    @GetMapping
    public ResponseEntity<Page<ClubSummaryResponse>> search(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "category", required = false) String category) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 50));
        return ResponseEntity.ok(clubService.searchClubs(keywords, category, pageable));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ClubSummaryResponse>> myClubs() {
        return ResponseEntity.ok(clubService.listMyClubs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClubResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.getClub(id));
    }

    @PostMapping
    public ResponseEntity<ClubResponse> create(@Valid @RequestBody ClubRequest request) {
        return ResponseEntity.ok(clubService.createClub(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClubResponse> update(
            @PathVariable Long id, @Valid @RequestBody ClubRequest request) {
        return ResponseEntity.ok(clubService.updateClub(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clubService.deleteClub(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recommendations")
    public ResponseEntity<Page<ClubSummaryResponse>> recommend(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "6") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 50));
        return ResponseEntity.ok(clubService.recommendClubs(pageable));
    }
}
