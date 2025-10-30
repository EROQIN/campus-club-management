package com.erokin.campusclubmanagement.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "请输入学号或邮箱")
    @Size(max = 120, message = "登录名长度不能超过120字符")
    private String identifier;

    @NotBlank(message = "请输入密码")
    private String password;
}

