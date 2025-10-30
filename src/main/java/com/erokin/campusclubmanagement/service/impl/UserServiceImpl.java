package com.erokin.campusclubmanagement.service.impl;

import com.erokin.campusclubmanagement.dto.user.UpdateProfileRequest;
import com.erokin.campusclubmanagement.dto.user.UserProfileResponse;
import com.erokin.campusclubmanagement.entity.InterestTag;
import com.erokin.campusclubmanagement.entity.User;
import com.erokin.campusclubmanagement.exception.BusinessException;
import com.erokin.campusclubmanagement.repository.InterestTagRepository;
import com.erokin.campusclubmanagement.repository.UserRepository;
import com.erokin.campusclubmanagement.service.UserService;
import com.erokin.campusclubmanagement.util.DtoMapper;
import com.erokin.campusclubmanagement.util.SecurityUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final InterestTagRepository interestTagRepository;
    private final DtoMapper dtoMapper;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        User user = getCurrentUser();
        return dtoMapper.toUserProfile(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateCurrentUserProfile(UpdateProfileRequest request) {
        User user = getCurrentUser();
        user.setBio(request.getBio());
        user.setAvatarUrl(request.getAvatarUrl());
        if (request.getInterestTags() != null && !request.getInterestTags().isEmpty()) {
            if (request.getInterestTags().size() > 5) {
                throw new BusinessException("兴趣标签最多选择5个");
            }
            user.setInterests(resolveTags(request.getInterestTags()));
        }
        return dtoMapper.toUserProfile(userRepository.save(user));
    }

    private User getCurrentUser() {
        Long id = SecurityUtils.getCurrentUserIdOrThrow();
        return userRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private Set<InterestTag> resolveTags(List<String> tagNames) {
        Set<InterestTag> tags = new HashSet<>();
        for (String name : tagNames) {
            if (!StringUtils.hasText(name)) {
                continue;
            }
            Optional<InterestTag> existing = interestTagRepository.findByNameIgnoreCase(name.trim());
            InterestTag tag = existing.orElseGet(() -> {
                InterestTag created = new InterestTag();
                created.setName(name.trim());
                return interestTagRepository.save(created);
            });
            tags.add(tag);
        }
        return tags;
    }
}

