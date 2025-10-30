package com.erokin.campusclubmanagement.dto.activity;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityRequest {

    @NotBlank(message = "活动标题不能为空")
    @Size(max = 150)
    private String title;

    @NotBlank(message = "活动描述不能为空")
    @Size(max = 800)
    private String description;

    @Size(max = 120)
    private String location;

    @FutureOrPresent(message = "开始时间不能早于当前")
    private Instant startTime;

    @Future(message = "结束时间必须在未来")
    private Instant endTime;

    private Integer capacity;

    @Size(max = 255)
    private String bannerUrl;

    private boolean requiresApproval = true;
}
