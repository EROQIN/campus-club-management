package com.erokin.campusclubmanagement.dto.admin;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRolesRequest {

    @NotEmpty(message = "请选择至少一个角色")
    private List<String> roles;
}

