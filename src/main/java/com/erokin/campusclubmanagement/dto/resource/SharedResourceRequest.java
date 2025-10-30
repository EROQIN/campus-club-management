package com.erokin.campusclubmanagement.dto.resource;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SharedResourceRequest {

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotBlank
    @Size(max = 60)
    private String resourceType;

    @Size(max = 500)
    private String description;

    private Instant availableFrom;
    private Instant availableUntil;

    @Size(max = 120)
    private String contact;
}

