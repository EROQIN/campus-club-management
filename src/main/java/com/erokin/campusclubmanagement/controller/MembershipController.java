package com.erokin.campusclubmanagement.controller;

import com.erokin.campusclubmanagement.dto.membership.MembershipAdminResponse;
import com.erokin.campusclubmanagement.dto.membership.MembershipDecisionRequest;
import com.erokin.campusclubmanagement.dto.membership.MembershipRequest;
import com.erokin.campusclubmanagement.dto.membership.MembershipResponse;
import com.erokin.campusclubmanagement.service.MembershipService;
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
@RequestMapping("/api/memberships")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @PostMapping
    public ResponseEntity<MembershipResponse> apply(
            @Valid @RequestBody MembershipRequest request) {
        return ResponseEntity.ok(membershipService.apply(request));
    }

    @PostMapping("/{id}/decision")
    public ResponseEntity<MembershipResponse> decide(
            @PathVariable Long id, @Valid @RequestBody MembershipDecisionRequest request) {
        return ResponseEntity.ok(membershipService.decide(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> withdraw(@PathVariable Long id) {
        membershipService.withdraw(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<MembershipResponse>> myMemberships() {
        return ResponseEntity.ok(membershipService.listMyMemberships());
    }

    @GetMapping("/clubs/{clubId}")
    @PreAuthorize("hasRole('CLUB_MANAGER') or hasRole('UNION_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<List<MembershipResponse>> clubApplicants(@PathVariable Long clubId) {
        return ResponseEntity.ok(membershipService.listClubApplicants(clubId));
    }

    @GetMapping("/clubs/{clubId}/members")
    @PreAuthorize("hasRole('CLUB_MANAGER') or hasRole('UNION_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<List<MembershipAdminResponse>> clubMembers(@PathVariable Long clubId) {
        return ResponseEntity.ok(membershipService.listClubMembers(clubId));
    }
}
