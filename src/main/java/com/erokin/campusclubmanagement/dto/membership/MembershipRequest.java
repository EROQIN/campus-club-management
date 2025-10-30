package com.erokin.campusclubmanagement.dto.membership;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipRequest {

    @NotNull
    private Long clubId;

    @Size(max = 300)
    private String applicationReason;
}

