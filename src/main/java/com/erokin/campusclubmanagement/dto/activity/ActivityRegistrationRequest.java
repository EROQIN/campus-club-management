package com.erokin.campusclubmanagement.dto.activity;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityRegistrationRequest {

    @Size(max = 300)
    private String note;
}

