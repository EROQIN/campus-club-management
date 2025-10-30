package com.erokin.campusclubmanagement.dto.club;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClubRequest {

    @NotBlank(message = "社团名称不能为空")
    @Size(max = 120, message = "社团名称不能超过120字符")
    private String name;

    @NotBlank(message = "社团简介不能为空")
    @Size(max = 500, message = "社团简介不能超过500字")
    private String description;

    @Size(max = 255)
    private String logoUrl;

    @Size(max = 255)
    private String promoVideoUrl;

    @Size(max = 60)
    private String category;

    @Email
    @Size(max = 120)
    private String contactEmail;

    @Size(max = 30)
    private String contactPhone;

    private LocalDate foundedDate;

    @Size(max = 10, message = "标签最多10个")
    private List<String> tags;
}

