package com.erokin.campusclubmanagement.dto.activity;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManualCheckInRequest {

    @NotEmpty(message = "请选择需要签到的成员")
    private List<Long> attendeeIds;
}
