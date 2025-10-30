package com.erokin.campusclubmanagement.dto.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceApplicationRequest {

    @NotBlank
    @Size(max = 400)
    private String purpose;

    private Instant requestedFrom;

    private Instant requestedUntil;
}

