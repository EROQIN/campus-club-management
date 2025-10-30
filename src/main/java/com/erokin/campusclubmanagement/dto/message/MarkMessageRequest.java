package com.erokin.campusclubmanagement.dto.message;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarkMessageRequest {

    @NotNull
    private Boolean read;
}

