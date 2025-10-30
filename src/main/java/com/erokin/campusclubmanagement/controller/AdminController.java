package com.erokin.campusclubmanagement.controller;

import com.erokin.campusclubmanagement.dto.admin.UpdateUserRolesRequest;
import com.erokin.campusclubmanagement.dto.admin.UpdateUserStatusRequest;
import com.erokin.campusclubmanagement.dto.admin.UserAdminResponse;
import com.erokin.campusclubmanagement.service.AdminService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserAdminResponse>> users(
            @RequestParam(value = "keyword", required = false) String keyword) {
        return ResponseEntity.ok(adminService.listUsers(keyword));
    }

    @PutMapping("/users/{id}/roles")
    public ResponseEntity<UserAdminResponse> updateRoles(
            @PathVariable Long id, @Valid @RequestBody UpdateUserRolesRequest request) {
        return ResponseEntity.ok(adminService.updateRoles(id, request));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<UserAdminResponse> updateStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(adminService.updateStatus(id, request));
    }
}

