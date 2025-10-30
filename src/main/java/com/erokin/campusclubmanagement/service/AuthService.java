package com.erokin.campusclubmanagement.service;

import com.erokin.campusclubmanagement.dto.auth.AuthResponse;
import com.erokin.campusclubmanagement.dto.auth.LoginRequest;
import com.erokin.campusclubmanagement.dto.auth.RegisterRequest;
import com.erokin.campusclubmanagement.dto.user.UserProfileResponse;
import java.util.List;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserProfileResponse getCurrentProfile();

    List<String> getAllTags();
}

