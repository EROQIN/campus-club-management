package com.erokin.campusclubmanagement.dto.membership;

import com.erokin.campusclubmanagement.enums.MembershipRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipDecisionRequest {

    @NotNull
    private Boolean approve;

    private MembershipRole membershipRole;

    @Size(max = 300)
    private String message;
}

