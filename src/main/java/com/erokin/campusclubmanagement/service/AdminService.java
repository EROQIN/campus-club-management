package com.erokin.campusclubmanagement.service;

import com.erokin.campusclubmanagement.dto.admin.UpdateUserRolesRequest;
import com.erokin.campusclubmanagement.dto.admin.UpdateUserStatusRequest;
import com.erokin.campusclubmanagement.dto.admin.UserAdminResponse;
import java.util.List;

public interface AdminService {

    List<UserAdminResponse> listUsers(String keyword);

    UserAdminResponse updateRoles(Long userId, UpdateUserRolesRequest request);

    UserAdminResponse updateStatus(Long userId, UpdateUserStatusRequest request);
}

