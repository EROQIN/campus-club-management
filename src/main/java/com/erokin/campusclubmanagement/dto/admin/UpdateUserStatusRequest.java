package com.erokin.campusclubmanagement.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserStatusRequest {

    @NotNull
    private Boolean enabled;
}

