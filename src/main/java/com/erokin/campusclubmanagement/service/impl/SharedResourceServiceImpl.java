package com.erokin.campusclubmanagement.service.impl;

import com.erokin.campusclubmanagement.dto.resource.SharedResourceRequest;
import com.erokin.campusclubmanagement.dto.resource.SharedResourceResponse;
import com.erokin.campusclubmanagement.entity.Club;
import com.erokin.campusclubmanagement.entity.SharedResource;
import com.erokin.campusclubmanagement.entity.User;
import com.erokin.campusclubmanagement.enums.Role;
import com.erokin.campusclubmanagement.exception.BusinessException;
import com.erokin.campusclubmanagement.exception.ResourceNotFoundException;
import com.erokin.campusclubmanagement.repository.ClubRepository;
import com.erokin.campusclubmanagement.repository.SharedResourceRepository;
import com.erokin.campusclubmanagement.repository.UserRepository;
import com.erokin.campusclubmanagement.service.SharedResourceService;
import com.erokin.campusclubmanagement.util.DtoMapper;
import com.erokin.campusclubmanagement.util.SecurityUtils;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SharedResourceServiceImpl implements SharedResourceService {

    private final SharedResourceRepository sharedResourceRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final DtoMapper dtoMapper;

    @Override
    @Transactional
    public SharedResourceResponse create(Long clubId, SharedResourceRequest request) {
        Club club = findClub(clubId);
        ensureManager(getCurrentUser(), club);
        SharedResource resource = new SharedResource();
        applyRequest(resource, request);
        resource.setClub(club);
        return dtoMapper.toSharedResourceResponse(sharedResourceRepository.save(resource));
    }

    @Override
    @Transactional
    public SharedResourceResponse update(Long resourceId, SharedResourceRequest request) {
        SharedResource resource =
                sharedResourceRepository
                        .findById(resourceId)
                        .orElseThrow(() -> new ResourceNotFoundException("资源不存在"));
        ensureManager(getCurrentUser(), resource.getClub());
        applyRequest(resource, request);
        return dtoMapper.toSharedResourceResponse(sharedResourceRepository.save(resource));
    }

    @Override
    @Transactional
    public void delete(Long resourceId) {
        SharedResource resource =
                sharedResourceRepository
                        .findById(resourceId)
                        .orElseThrow(() -> new ResourceNotFoundException("资源不存在"));
        ensureManager(getCurrentUser(), resource.getClub());
        sharedResourceRepository.delete(resource);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SharedResourceResponse> listAll() {
        return sharedResourceRepository.findAll().stream()
                .map(dtoMapper::toSharedResourceResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SharedResourceResponse> listByClub(Long clubId) {
        Club club = findClub(clubId);
        return sharedResourceRepository.findByClub(club).stream()
                .map(dtoMapper::toSharedResourceResponse)
                .toList();
    }

    private void applyRequest(SharedResource resource, SharedResourceRequest request) {
        resource.setName(request.getName());
        resource.setResourceType(request.getResourceType());
        resource.setDescription(request.getDescription());
        resource.setAvailableFrom(request.getAvailableFrom());
        resource.setAvailableUntil(request.getAvailableUntil());
        resource.setContact(request.getContact());
    }

    private Club findClub(Long clubId) {
        return clubRepository
                .findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("社团不存在"));
    }

    private void ensureManager(User user, Club club) {
        if (club.getCreatedBy() != null && club.getCreatedBy().getId().equals(user.getId())) {
            return;
        }
        if (user.getRoles().contains(Role.SYSTEM_ADMIN) || user.getRoles().contains(Role.UNION_STAFF)) {
            return;
        }
        throw new BusinessException("没有权限管理该资源");
    }

    private User getCurrentUser() {
        Long id = SecurityUtils.getCurrentUserIdOrThrow();
        return userRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
    }
}

