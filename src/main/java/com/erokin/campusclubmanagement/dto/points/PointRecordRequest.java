package com.erokin.campusclubmanagement.dto.points;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointRecordRequest {

    @NotNull(message = "请选择成员")
    private Long memberId;

    private int points;

    @NotBlank(message = "请填写积分变动原因")
    private String reason;
}
