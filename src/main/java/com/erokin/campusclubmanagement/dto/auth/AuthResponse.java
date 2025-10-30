package com.erokin.campusclubmanagement.dto.auth;

import com.erokin.campusclubmanagement.dto.user.UserSummary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private long expiresIn;
    private UserSummary user;
}

