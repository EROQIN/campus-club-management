package com.erokin.campusclubmanagement.dto.admin;

import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAdminResponse {
    private Long id;
    private String fullName;
    private String email;
    private String studentNumber;
    private boolean enabled;
    private List<String> roles;
    private Instant createdAt;
    private Instant lastLoginAt;
}

