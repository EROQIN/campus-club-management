package com.erokin.campusclubmanagement.dto.activity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityArchiveRequest {

    @NotBlank
    @Size(max = 4000, message = "活动总结内容不能超过 4000 字符")
    private String summary;

    @Size(max = 50, message = "最多上传 50 张活动照片")
    private List<@NotBlank @Size(max = 512) String> photoUrls = new ArrayList<>();
}
