package com.erokin.campusclubmanagement.dto.task;

import com.erokin.campusclubmanagement.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusUpdateRequest {

    @NotNull(message = "请选择状态")
    private TaskStatus status;

    private String remark;
}
