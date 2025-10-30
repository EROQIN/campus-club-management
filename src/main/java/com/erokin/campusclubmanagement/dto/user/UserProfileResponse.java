package com.erokin.campusclubmanagement.dto.user;

import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileResponse {
    private Long id;
    private String studentNumber;
    private String fullName;
    private String email;
    private String bio;
    private String avatarUrl;
    private boolean enabled;
    private List<String> roles;
    private List<String> interests;
    private Instant createdAt;
}

