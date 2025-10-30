package com.erokin.campusclubmanagement.dto.user;

import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @Size(max = 500, message = "个人简介不能超过500字")
    private String bio;

    @Size(max = 255)
    private String avatarUrl;

    @Size(max = 5, message = "兴趣标签最多选择5个")
    private List<String> interestTags;
}

