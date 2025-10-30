package com.erokin.campusclubmanagement.dto.collaboration;

import com.erokin.campusclubmanagement.enums.CollaborationResponseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollaborationResponseRequest {

    @NotBlank(message = "请输入回复内容")
    private String message;

    @NotNull(message = "请选择回复状态")
    private CollaborationResponseStatus status;
}
