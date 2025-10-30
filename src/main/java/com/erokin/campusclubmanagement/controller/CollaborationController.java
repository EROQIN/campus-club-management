package com.erokin.campusclubmanagement.controller;

import com.erokin.campusclubmanagement.dto.collaboration.CollaborationProposalRequest;
import com.erokin.campusclubmanagement.dto.collaboration.CollaborationProposalResponse;
import com.erokin.campusclubmanagement.dto.collaboration.CollaborationResponseRequest;
import com.erokin.campusclubmanagement.service.CollaborationService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/collaborations")
public class CollaborationController {

    private final CollaborationService collaborationService;

    public CollaborationController(CollaborationService collaborationService) {
        this.collaborationService = collaborationService;
    }

    @PostMapping("/clubs/{clubId}")
    public ResponseEntity<CollaborationProposalResponse> createProposal(
            @PathVariable Long clubId, @Valid @RequestBody CollaborationProposalRequest request) {
        return ResponseEntity.ok(collaborationService.createProposal(clubId, request));
    }

    @GetMapping("/clubs/{clubId}")
    public ResponseEntity<Page<CollaborationProposalResponse>> listInitiated(
            @PathVariable Long clubId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 50));
        return ResponseEntity.ok(collaborationService.listInitiated(clubId, pageable));
    }

    @GetMapping
    public ResponseEntity<Page<CollaborationProposalResponse>> listAll(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 50));
        return ResponseEntity.ok(collaborationService.listAll(pageable));
    }

    @PostMapping("/{proposalId}/respond/{clubId}")
    public ResponseEntity<CollaborationProposalResponse> respond(
            @PathVariable Long proposalId,
            @PathVariable Long clubId,
            @Valid @RequestBody CollaborationResponseRequest request) {
        return ResponseEntity.ok(collaborationService.respond(proposalId, clubId, request));
    }
}
