package com.erokin.campusclubmanagement.service.impl;

import com.erokin.campusclubmanagement.dto.activity.ActivityRegistrationRequest;
import com.erokin.campusclubmanagement.dto.activity.ActivityRegistrationResponse;
import com.erokin.campusclubmanagement.dto.activity.ActivityRequest;
import com.erokin.campusclubmanagement.dto.activity.ActivityResponse;
import com.erokin.campusclubmanagement.dto.activity.ActivitySummaryResponse;
import com.erokin.campusclubmanagement.entity.Activity;
import com.erokin.campusclubmanagement.entity.ActivityRegistration;
import com.erokin.campusclubmanagement.entity.Club;
import com.erokin.campusclubmanagement.entity.Message;
import com.erokin.campusclubmanagement.entity.User;
import com.erokin.campusclubmanagement.enums.ActivityRegistrationStatus;
import com.erokin.campusclubmanagement.enums.MessageType;
import com.erokin.campusclubmanagement.enums.Role;
import com.erokin.campusclubmanagement.exception.BusinessException;
import com.erokin.campusclubmanagement.exception.ResourceNotFoundException;
import com.erokin.campusclubmanagement.repository.ActivityRegistrationRepository;
import com.erokin.campusclubmanagement.repository.ActivityRepository;
import com.erokin.campusclubmanagement.repository.ClubRepository;
import com.erokin.campusclubmanagement.repository.MessageRepository;
import com.erokin.campusclubmanagement.repository.UserRepository;
import com.erokin.campusclubmanagement.service.ActivityService;
import com.erokin.campusclubmanagement.util.DtoMapper;
import com.erokin.campusclubmanagement.util.SecurityUtils;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ClubRepository clubRepository;
    private final ActivityRepository activityRepository;
    private final ActivityRegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final DtoMapper dtoMapper;

    @Override
    public ActivityResponse createActivity(Long clubId, ActivityRequest request) {
        Club club = findClub(clubId);
        User current = getCurrentUser();
        ensureManager(current, club);

        Activity activity = new Activity();
        applyRequest(activity, request);
        activity.setClub(club);

        Activity saved = activityRepository.save(activity);
        return dtoMapper.toActivityResponse(saved, 0);
    }

    @Override
    public ActivityResponse updateActivity(Long activityId, ActivityRequest request) {
        Activity activity = findActivity(activityId);
        User current = getCurrentUser();
        ensureManager(current, activity.getClub());
        applyRequest(activity, request);
        return dtoMapper.toActivityResponse(activityRepository.save(activity),
                (int) registrationRepository.countByActivity(activity));
    }

    @Override
    public void deleteActivity(Long activityId) {
        Activity activity = findActivity(activityId);
        User current = getCurrentUser();
        ensureManager(current, activity.getClub());
        activityRepository.delete(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityResponse getActivity(Long activityId) {
        Activity activity = findActivity(activityId);
        return dtoMapper.toActivityResponse(activity,
                (int) registrationRepository.countByActivity(activity));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivitySummaryResponse> listClubActivities(Long clubId, Pageable pageable) {
        Club club = findClub(clubId);
        return activityRepository.findByClub(club, pageable)
                .map(activity ->
                        dtoMapper.toActivitySummary(
                                activity,
                                (int) registrationRepository.countByActivity(activity)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivitySummaryResponse> listUpcomingActivities(Pageable pageable) {
        return activityRepository.findByStartTimeAfter(Instant.now(), pageable)
                .map(activity ->
                        dtoMapper.toActivitySummary(
                                activity,
                                (int) registrationRepository.countByActivity(activity)));
    }

    @Override
    public ActivityRegistrationResponse registerForActivity(
            Long activityId, ActivityRegistrationRequest request) {
        Activity activity = findActivity(activityId);
        User student = getCurrentUser();
        registrationRepository
                .findByActivityAndAttendee(activity, student)
                .ifPresent(existing -> {
                    throw new BusinessException("已报名该活动");
                });

        if (activity.getCapacity() != null) {
            long count = registrationRepository.countByActivity(activity);
            if (count >= activity.getCapacity()) {
                throw new BusinessException("活动报名人数已满");
            }
        }

        ActivityRegistration registration = new ActivityRegistration();
        registration.setActivity(activity);
        registration.setAttendee(student);
        registration.setNote(request.getNote());
        registration.setStatus(
                activity.isRequiresApproval()
                        ? ActivityRegistrationStatus.PENDING
                        : ActivityRegistrationStatus.APPROVED);

        ActivityRegistration saved = registrationRepository.save(registration);
        notifyManager(
                activity.getClub(),
                "新的活动报名",
                student.getFullName() + " 报名参加 " + activity.getTitle(),
                saved.getId());
        return dtoMapper.toActivityRegistrationResponse(saved);
    }

    @Override
    public ActivityRegistrationResponse reviewRegistration(Long registrationId, boolean approve) {
        ActivityRegistration registration =
                registrationRepository
                        .findById(registrationId)
                        .orElseThrow(() -> new ResourceNotFoundException("报名记录不存在"));
        User current = getCurrentUser();
        ensureManager(current, registration.getActivity().getClub());
        registration.setStatus(
                approve
                        ? ActivityRegistrationStatus.APPROVED
                        : ActivityRegistrationStatus.DECLINED);
        ActivityRegistration saved = registrationRepository.save(registration);
        notifyUser(
                registration.getAttendee(),
                approve ? "活动报名已通过" : "活动报名被拒绝",
                registration.getActivity().getTitle(),
                saved.getId());
        return dtoMapper.toActivityRegistrationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityRegistrationResponse> listRegistrationsForActivity(
            Long activityId, Pageable pageable) {
        Activity activity = findActivity(activityId);
        User current = getCurrentUser();
        ensureManager(current, activity.getClub());
        return registrationRepository.findByActivity(activity, pageable)
                .map(dtoMapper::toActivityRegistrationResponse);
    }

    private Club findClub(Long clubId) {
        return clubRepository
                .findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("社团不存在"));
    }

    private Activity findActivity(Long activityId) {
        return activityRepository
                .findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("活动不存在"));
    }

    private User getCurrentUser() {
        Long id = SecurityUtils.getCurrentUserIdOrThrow();
        return userRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private void ensureManager(User user, Club club) {
        if (club.getCreatedBy() != null && club.getCreatedBy().getId().equals(user.getId())) {
            return;
        }
        if (user.getRoles().contains(Role.SYSTEM_ADMIN) || user.getRoles().contains(Role.UNION_STAFF)) {
            return;
        }
        throw new BusinessException("没有权限管理该活动");
    }

    private void applyRequest(Activity activity, ActivityRequest request) {
        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setLocation(request.getLocation());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setCapacity(request.getCapacity());
        activity.setBannerUrl(request.getBannerUrl());
        activity.setRequiresApproval(request.isRequiresApproval());
    }

    private void notifyManager(Club club, String title, String content, Long referenceId) {
        if (club.getCreatedBy() == null) {
            return;
        }
        Message message = new Message();
        message.setRecipient(club.getCreatedBy());
        message.setType(MessageType.ACTIVITY);
        message.setTitle(title);
        message.setContent(content);
        message.setReferenceType("ACTIVITY_REGISTRATION");
        message.setReferenceId(referenceId);
        messageRepository.save(message);
    }

    private void notifyUser(User user, String title, String activityTitle, Long referenceId) {
        Message message = new Message();
        message.setRecipient(user);
        message.setType(MessageType.ACTIVITY);
        message.setTitle(title);
        message.setContent(activityTitle);
        message.setReferenceType("ACTIVITY_REGISTRATION");
        message.setReferenceId(referenceId);
        messageRepository.save(message);
    }
}
