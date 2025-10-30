package com.erokin.campusclubmanagement.controller;

import com.erokin.campusclubmanagement.dto.auth.AuthResponse;
import com.erokin.campusclubmanagement.dto.auth.LoginRequest;
import com.erokin.campusclubmanagement.dto.auth.RegisterRequest;
import com.erokin.campusclubmanagement.dto.user.UserProfileResponse;
import com.erokin.campusclubmanagement.service.AuthService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> currentProfile() {
        return ResponseEntity.ok(authService.getCurrentProfile());
    }

    @GetMapping("/tags")
    public ResponseEntity<List<String>> listTags() {
        return ResponseEntity.ok(authService.getAllTags());
    }
}

