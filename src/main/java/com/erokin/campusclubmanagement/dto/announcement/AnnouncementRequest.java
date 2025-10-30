package com.erokin.campusclubmanagement.dto.announcement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnouncementRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 150)
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 800)
    private String content;
}

