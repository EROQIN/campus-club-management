package com.erokin.campusclubmanagement.dto.activity;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityCheckInRequest {

    @NotBlank(message = "签到凭证无效")
    private String token;

    private String method; // optional override for manual check-in etc.
}
