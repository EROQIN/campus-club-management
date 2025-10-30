package com.erokin.campusclubmanagement.service.impl;

import com.erokin.campusclubmanagement.dto.auth.AuthResponse;
import com.erokin.campusclubmanagement.dto.auth.LoginRequest;
import com.erokin.campusclubmanagement.dto.auth.RegisterRequest;
import com.erokin.campusclubmanagement.dto.user.UserProfileResponse;
import com.erokin.campusclubmanagement.entity.InterestTag;
import com.erokin.campusclubmanagement.entity.User;
import com.erokin.campusclubmanagement.enums.Role;
import com.erokin.campusclubmanagement.exception.BusinessException;
import com.erokin.campusclubmanagement.repository.InterestTagRepository;
import com.erokin.campusclubmanagement.repository.UserRepository;
import com.erokin.campusclubmanagement.security.UserPrincipal;
import com.erokin.campusclubmanagement.security.jwt.JwtService;
import com.erokin.campusclubmanagement.service.AuthService;
import com.erokin.campusclubmanagement.util.DtoMapper;
import com.erokin.campusclubmanagement.util.SecurityUtils;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final InterestTagRepository interestTagRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final DtoMapper dtoMapper;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BusinessException("该邮箱已注册");
        }
        if (request.getStudentNumber() != null
                && userRepository.existsByStudentNumber(request.getStudentNumber())) {
            throw new BusinessException("该学号已注册");
        }
        if (CollectionUtils.isEmpty(request.getInterestTags())) {
            throw new BusinessException("请至少选择三个兴趣标签");
        }

        User user = new User();
        user.setStudentNumber(request.getStudentNumber());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail().toLowerCase(Locale.CHINA));
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(buildRoles(request.isRegisteringAsManager()));
        user.setInterests(resolveTags(request.getInterestTags()));

        User saved = userRepository.save(user);
        UserPrincipal principal = UserPrincipal.fromUser(saved);
        String token = jwtService.generateToken(principal);

        return new AuthResponse(token, jwtService.getExpirationMs(), dtoMapper.toUserSummary(saved));
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getIdentifier(), request.getPassword()));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user =
                userRepository
                        .findById(principal.getId())
                        .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        String token = jwtService.generateToken(principal);

        return new AuthResponse(token, jwtService.getExpirationMs(), dtoMapper.toUserSummary(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentProfile() {
        Long userId = SecurityUtils.getCurrentUserIdOrThrow();
        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new BusinessException("用户不存在"));
        return dtoMapper.toUserProfile(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllTags() {
        return interestTagRepository.findAll().stream().map(InterestTag::getName).toList();
    }

    private Set<Role> buildRoles(boolean isManager) {
        Set<Role> roles = new HashSet<>();
        roles.add(Role.STUDENT);
        if (isManager) {
            roles.add(Role.CLUB_MANAGER);
        }
        return roles;
    }

    private Set<InterestTag> resolveTags(List<String> tagNames) {
        Set<InterestTag> tags = new HashSet<>();
        for (String name : tagNames) {
            if (name == null || name.isBlank()) {
                continue;
            }
            InterestTag tag =
                    interestTagRepository
                            .findByNameIgnoreCase(name.trim())
                            .orElseGet(
                                    () -> {
                                        InterestTag newTag = new InterestTag();
                                        newTag.setName(name.trim());
                                        return interestTagRepository.save(newTag);
                                    });
            tags.add(tag);
        }
        return tags;
    }
}
