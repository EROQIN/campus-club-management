package com.erokin.campusclubmanagement.service.impl;

import com.erokin.campusclubmanagement.dto.resource.ResourceApplicationDecisionRequest;
import com.erokin.campusclubmanagement.dto.resource.ResourceApplicationRequest;
import com.erokin.campusclubmanagement.dto.resource.ResourceApplicationResponse;
import com.erokin.campusclubmanagement.entity.Message;
import com.erokin.campusclubmanagement.entity.ResourceApplication;
import com.erokin.campusclubmanagement.entity.SharedResource;
import com.erokin.campusclubmanagement.entity.User;
import com.erokin.campusclubmanagement.enums.MessageType;
import com.erokin.campusclubmanagement.enums.ResourceApplicationStatus;
import com.erokin.campusclubmanagement.enums.Role;
import com.erokin.campusclubmanagement.exception.BusinessException;
import com.erokin.campusclubmanagement.exception.ResourceNotFoundException;
import com.erokin.campusclubmanagement.repository.MessageRepository;
import com.erokin.campusclubmanagement.repository.ResourceApplicationRepository;
import com.erokin.campusclubmanagement.repository.SharedResourceRepository;
import com.erokin.campusclubmanagement.repository.UserRepository;
import com.erokin.campusclubmanagement.service.ResourceApplicationService;
import com.erokin.campusclubmanagement.util.DtoMapper;
import com.erokin.campusclubmanagement.util.SecurityUtils;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ResourceApplicationServiceImpl implements ResourceApplicationService {

    private final SharedResourceRepository sharedResourceRepository;
    private final ResourceApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final DtoMapper dtoMapper;

    @Override
    @Transactional
    public ResourceApplicationResponse apply(Long resourceId, ResourceApplicationRequest request) {
        SharedResource resource = findResource(resourceId);
        User applicant = getCurrentUser();

        if (resource.getClub().getCreatedBy() != null
                && resource.getClub().getCreatedBy().getId().equals(applicant.getId())) {
            throw new BusinessException("资源发布者无需申请使用");
        }

        if (!StringUtils.hasText(request.getPurpose())) {
            throw new BusinessException("请填写申请用途");
        }

        ResourceApplication application = new ResourceApplication();
        application.setResource(resource);
        application.setApplicant(applicant);
        application.setPurpose(request.getPurpose().trim());
        application.setRequestedFrom(request.getRequestedFrom());
        application.setRequestedUntil(request.getRequestedUntil());
        application.setStatus(ResourceApplicationStatus.PENDING);

        ResourceApplication saved = applicationRepository.save(application);
        notifyOwner(resource, saved);
        return dtoMapper.toResourceApplicationResponse(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('CLUB_MANAGER') or hasRole('UNION_STAFF') or hasRole('SYSTEM_ADMIN')")
    public ResourceApplicationResponse decide(
            Long applicationId, ResourceApplicationDecisionRequest request) {
        ResourceApplication application =
                applicationRepository
                        .findById(applicationId)
                        .orElseThrow(() -> new ResourceNotFoundException("申请不存在"));
        User current = getCurrentUser();
        ensureResourceOwner(application.getResource(), current);

        if (application.getStatus() != ResourceApplicationStatus.PENDING) {
            throw new BusinessException("该申请已处理");
        }

        application.setStatus(request.resolveStatus());
        application.setRespondedAt(Instant.now());
        application.setReplyMessage(request.getReplyMessage());
        applicationRepository.save(application);
        notifyApplicant(application);
        return dtoMapper.toResourceApplicationResponse(application);
    }

    @Override
    @Transactional
    public void cancel(Long applicationId) {
        ResourceApplication application =
                applicationRepository
                        .findById(applicationId)
                        .orElseThrow(() -> new ResourceNotFoundException("申请不存在"));
        User current = getCurrentUser();
        if (!application.getApplicant().getId().equals(current.getId())) {
            throw new BusinessException("只能取消自己的申请");
        }
        if (application.getStatus() != ResourceApplicationStatus.PENDING) {
            throw new BusinessException("该申请已处理，无法撤回");
        }
        application.setStatus(ResourceApplicationStatus.CANCELLED);
        application.setRespondedAt(Instant.now());
        applicationRepository.save(application);
        notifyOwnerCancellation(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceApplicationResponse> listMyApplications() {
        User current = getCurrentUser();
        return applicationRepository.findByApplicant(current).stream()
                .map(dtoMapper::toResourceApplicationResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('CLUB_MANAGER') or hasRole('UNION_STAFF') or hasRole('SYSTEM_ADMIN')")
    public List<ResourceApplicationResponse> listApplicationsForResource(Long resourceId) {
        SharedResource resource = findResource(resourceId);
        ensureResourceOwner(resource, getCurrentUser());
        return applicationRepository.findByResource(resource).stream()
                .map(dtoMapper::toResourceApplicationResponse)
                .toList();
    }

    private SharedResource findResource(Long resourceId) {
        return sharedResourceRepository
                .findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("资源不存在"));
    }

    private User getCurrentUser() {
        Long id = SecurityUtils.getCurrentUserIdOrThrow();
        return userRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private void ensureResourceOwner(SharedResource resource, User user) {
        if (resource.getClub().getCreatedBy() != null
                && resource.getClub().getCreatedBy().getId().equals(user.getId())) {
            return;
        }
        if (user.getRoles().contains(Role.SYSTEM_ADMIN) || user.getRoles().contains(Role.UNION_STAFF)) {
            return;
        }
        throw new BusinessException("没有权限处理该资源申请");
    }

    private void notifyOwner(SharedResource resource, ResourceApplication application) {
        User recipient = resource.getClub().getCreatedBy();
        if (recipient == null) {
            return;
        }
        Message message = new Message();
        message.setRecipient(recipient);
        message.setType(MessageType.ANNOUNCEMENT);
        message.setTitle("新的资源申请");
        message.setContent(
                application.getApplicant().getFullName() + " 申请使用 " + resource.getName());
        message.setReferenceType("RESOURCE_APPLICATION");
        message.setReferenceId(application.getId());
        messageRepository.save(message);
    }

    private void notifyApplicant(ResourceApplication application) {
        Message message = new Message();
        message.setRecipient(application.getApplicant());
        message.setType(MessageType.ANNOUNCEMENT);
        message.setTitle("资源申请处理结果");
        message.setContent(
                String.format(
                        "%s：%s",
                        application.getResource().getName(), application.getStatus().name()));
        message.setReferenceType("RESOURCE_APPLICATION");
        message.setReferenceId(application.getId());
        messageRepository.save(message);
    }

    private void notifyOwnerCancellation(ResourceApplication application) {
        SharedResource resource = application.getResource();
        if (resource.getClub().getCreatedBy() == null) {
            return;
        }
        Message message = new Message();
        message.setRecipient(resource.getClub().getCreatedBy());
        message.setType(MessageType.ANNOUNCEMENT);
        message.setTitle("资源申请已撤回");
        message.setContent(
                application.getApplicant().getFullName()
                        + " 撤回了对资源 "
                        + resource.getName()
                        + " 的申请");
        message.setReferenceType("RESOURCE_APPLICATION");
        message.setReferenceId(application.getId());
        messageRepository.save(message);
    }
}

