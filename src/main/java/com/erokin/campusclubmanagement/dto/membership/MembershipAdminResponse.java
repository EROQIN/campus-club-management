package com.erokin.campusclubmanagement.dto.membership;

import com.erokin.campusclubmanagement.enums.MembershipRole;
import com.erokin.campusclubmanagement.enums.MembershipStatus;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipAdminResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private MembershipStatus status;
    private MembershipRole membershipRole;
    private String applicationReason;
    private Instant createdAt;
    private Instant respondedAt;
}

