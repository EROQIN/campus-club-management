package com.erokin.campusclubmanagement.service.impl;

import com.erokin.campusclubmanagement.dto.admin.UpdateUserRolesRequest;
import com.erokin.campusclubmanagement.dto.admin.UpdateUserStatusRequest;
import com.erokin.campusclubmanagement.dto.admin.UserAdminResponse;
import com.erokin.campusclubmanagement.entity.User;
import com.erokin.campusclubmanagement.enums.Role;
import com.erokin.campusclubmanagement.exception.BusinessException;
import com.erokin.campusclubmanagement.exception.ResourceNotFoundException;
import com.erokin.campusclubmanagement.repository.UserRepository;
import com.erokin.campusclubmanagement.service.AdminService;
import com.erokin.campusclubmanagement.util.DtoMapper;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final DtoMapper dtoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserAdminResponse> listUsers(String keyword) {
        List<User> users = userRepository.findAll();
        if (StringUtils.hasText(keyword)) {
            String lower = keyword.toLowerCase(Locale.ROOT);
            users =
                    users.stream()
                            .filter(
                                    user ->
                                            containsIgnoreCase(user.getFullName(), lower)
                                                    || containsIgnoreCase(user.getEmail(), lower)
                                                    || containsIgnoreCase(user.getStudentNumber(), lower))
                            .collect(Collectors.toList());
        }
        return users.stream().map(dtoMapper::toUserAdminResponse).toList();
    }

    @Override
    @Transactional
    public UserAdminResponse updateRoles(Long userId, UpdateUserRolesRequest request) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        Set<Role> roles = toRoles(request.getRoles());
        if (roles.isEmpty()) {
            throw new BusinessException("至少保留一个角色");
        }
        user.setRoles(roles);
        return dtoMapper.toUserAdminResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserAdminResponse updateStatus(Long userId, UpdateUserStatusRequest request) {
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        user.setEnabled(Boolean.TRUE.equals(request.getEnabled()));
        return dtoMapper.toUserAdminResponse(userRepository.save(user));
    }

    private boolean containsIgnoreCase(String source, String token) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(token);
    }

    private Set<Role> toRoles(List<String> roles) {
        EnumSet<Role> enumSet = EnumSet.noneOf(Role.class);
        for (String role : roles) {
            try {
                enumSet.add(Role.valueOf(role));
            } catch (IllegalArgumentException e) {
                throw new BusinessException("角色无效: " + role);
            }
        }
        return enumSet;
    }
}

