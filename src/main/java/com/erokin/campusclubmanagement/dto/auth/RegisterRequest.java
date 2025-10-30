package com.erokin.campusclubmanagement.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @Pattern(
            regexp = "^[0-9]{11}$",
            message = "学号需为11位数字")
    private String studentNumber;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 80, message = "姓名不能超过80字符")
    private String fullName;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 120, message = "邮箱不能超过120字符")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "密码至少8位，且包含字母和数字")
    private String password;

    @Size(min = 3, max = 5, message = "兴趣标签需选择3-5个")
    private List<@NotBlank String> interestTags;

    private boolean registeringAsManager = false;
}

