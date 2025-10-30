package com.erokin.campusclubmanagement.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClubTaskRequest {

    @NotBlank(message = "请输入任务标题")
    @Size(max = 150, message = "标题长度不超过150字")
    private String title;

    @Size(max = 1000, message = "任务描述不超过1000字")
    private String description;

    private Instant dueAt;

    private List<Long> assigneeIds;
}
