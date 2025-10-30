package com.erokin.campusclubmanagement.service.impl;

import com.erokin.campusclubmanagement.dto.activity.ActivityCheckInRequest;
import com.erokin.campusclubmanagement.dto.activity.ActivityCheckInResponse;
import com.erokin.campusclubmanagement.dto.activity.ActivityRegistrationRequest;
import com.erokin.campusclubmanagement.dto.activity.ActivityRegistrationResponse;
import com.erokin.campusclubmanagement.dto.activity.ActivityRequest;
import com.erokin.campusclubmanagement.dto.activity.ActivityResponse;
import com.erokin.campusclubmanagement.dto.activity.ActivitySummaryResponse;
import com.erokin.campusclubmanagement.dto.activity.CheckInQrResponse;
import com.erokin.campusclubmanagement.dto.activity.ManualCheckInRequest;
import com.erokin.campusclubmanagement.entity.Activity;
import com.erokin.campusclubmanagement.entity.ActivityCheckIn;
import com.erokin.campusclubmanagement.entity.ActivityRegistration;
import com.erokin.campusclubmanagement.entity.Club;
import com.erokin.campusclubmanagement.entity.Message;
import com.erokin.campusclubmanagement.entity.User;
import com.erokin.campusclubmanagement.enums.ActivityRegistrationStatus;
import com.erokin.campusclubmanagement.enums.MessageType;
import com.erokin.campusclubmanagement.enums.Role;
import com.erokin.campusclubmanagement.enums.CheckInMethod;
import com.erokin.campusclubmanagement.exception.BusinessException;
import com.erokin.campusclubmanagement.exception.ResourceNotFoundException;
import com.erokin.campusclubmanagement.repository.ActivityRegistrationRepository;
import com.erokin.campusclubmanagement.repository.ActivityRepository;
import com.erokin.campusclubmanagement.repository.ActivityCheckInRepository;
import com.erokin.campusclubmanagement.repository.ClubRepository;
import com.erokin.campusclubmanagement.repository.MessageRepository;
import com.erokin.campusclubmanagement.repository.UserRepository;
import com.erokin.campusclubmanagement.service.FileStorageService;
import com.erokin.campusclubmanagement.service.QrCodeService;
import com.erokin.campusclubmanagement.service.ActivityService;
import com.erokin.campusclubmanagement.util.DtoMapper;
import com.erokin.campusclubmanagement.util.SecurityUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
@Transactional
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ClubRepository clubRepository;
    private final ActivityRepository activityRepository;
    private final ActivityRegistrationRepository registrationRepository;
    private final ActivityCheckInRepository checkInRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final DtoMapper dtoMapper;
    private final FileStorageService fileStorageService;
    private final QrCodeService qrCodeService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendBaseUrl;

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

    @Override
    public CheckInQrResponse generateCheckInQr(Long activityId) {
        Activity activity = findActivity(activityId);
        User current = getCurrentUser();
        ensureManager(current, activity.getClub());

        Instant now = Instant.now();
        Instant expiresAt = activity.getEndTime() != null
                ? activity.getEndTime()
                : now.plus(Duration.ofHours(12));
        if (expiresAt.isBefore(now)) {
            throw new BusinessException("活动已结束，无法生成签到码");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        String payload = buildCheckInPayload(activityId, token, expiresAt);
        byte[] qrImage = qrCodeService.generatePng(payload, 420);
        String filename = "checkin-" + activityId + "-" + now.toEpochMilli() + ".png";

        var stored = fileStorageService.upload(
                "checkins/" + activityId,
                filename,
                new ByteArrayInputStream(qrImage),
                qrImage.length,
                "image/png");

        activity.setCheckInToken(token);
        activity.setCheckInTokenExpiresAt(expiresAt);
        activity.setCheckInQrGeneratedAt(now);
        activity.setCheckInQrUrl(stored.url());
        activityRepository.save(activity);

        return CheckInQrResponse.builder()
                .activityId(activityId)
                .qrUrl(stored.url())
                .token(token)
                .expiresAt(expiresAt)
                .generatedAt(now)
                .payload(payload)
                .build();
    }

    @Override
    public ActivityCheckInResponse checkIn(Long activityId, ActivityCheckInRequest request) {
        Activity activity = findActivity(activityId);
        User attendee = getCurrentUser();

        validateCheckInToken(activity, request.getToken());

        ActivityRegistration registration = registrationRepository
                .findByActivityAndAttendee(activity, attendee)
                .orElseThrow(() -> new BusinessException("请先报名活动，再完成签到"));

        if (activity.isRequiresApproval()
                && registration.getStatus() != ActivityRegistrationStatus.APPROVED
                && registration.getStatus() != ActivityRegistrationStatus.ATTENDED) {
            throw new BusinessException("报名尚未通过，暂无法签到");
        }

        if (checkInRepository.existsByActivityAndAttendee(activity, attendee)) {
            throw new BusinessException("您已完成签到，无需重复操作");
        }

        ActivityCheckIn checkIn = new ActivityCheckIn();
        checkIn.setActivity(activity);
        checkIn.setAttendee(attendee);
        checkIn.setCheckedInAt(Instant.now());
        checkIn.setMethod(resolveMethod(request.getMethod()));

        ActivityCheckIn saved = checkInRepository.save(checkIn);
        registration.setStatus(ActivityRegistrationStatus.ATTENDED);
        registrationRepository.save(registration);

        return dtoMapper.toActivityCheckInResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityCheckInResponse> listCheckIns(Long activityId, Pageable pageable) {
        Activity activity = findActivity(activityId);
        User current = getCurrentUser();
        ensureManager(current, activity.getClub());
        return checkInRepository.findByActivity(activity, pageable)
                .map(dtoMapper::toActivityCheckInResponse);
    }

    @Override
    public Page<ActivityCheckInResponse> manualCheckIn(
            Long activityId, ManualCheckInRequest request, Pageable pageable) {
        Activity activity = findActivity(activityId);
        User current = getCurrentUser();
        ensureManager(current, activity.getClub());

        if (request.getAttendeeIds() == null || request.getAttendeeIds().isEmpty()) {
            throw new BusinessException("请选择需要签到的成员");
        }

        List<User> attendees = userRepository.findAllById(request.getAttendeeIds());
        if (attendees.isEmpty()) {
            throw new BusinessException("未找到有效的成员信息");
        }

        Map<Long, ActivityRegistration> registrationMap = new HashMap<>();
        registrationRepository
                .findByActivity(activity, Pageable.unpaged())
                .forEach(reg -> registrationMap.put(reg.getAttendee().getId(), reg));

        Instant now = Instant.now();
        attendees.forEach(user -> {
            ActivityRegistration registration = registrationMap.get(user.getId());
            if (registration == null) {
                throw new BusinessException(user.getFullName() + " 尚未报名该活动，无法签到");
            }
            if (checkInRepository.existsByActivityAndAttendee(activity, user)) {
                return;
            }
            ActivityCheckIn checkIn = new ActivityCheckIn();
            checkIn.setActivity(activity);
            checkIn.setAttendee(user);
            checkIn.setCheckedInAt(now);
            checkIn.setMethod(CheckInMethod.MANUAL);
            checkInRepository.save(checkIn);

            registration.setStatus(ActivityRegistrationStatus.ATTENDED);
            registrationRepository.save(registration);
        });

        return listCheckIns(activityId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportAttendance(Long activityId) {
        Activity activity = findActivity(activityId);
        User current = getCurrentUser();
        ensureManager(current, activity.getClub());

        List<ActivityRegistration> registrations =
                registrationRepository.findByActivity(activity, Pageable.unpaged()).getContent();
        Map<Long, ActivityCheckIn> checkInByUser = new HashMap<>();
        checkInRepository.findByActivity(activity, Pageable.unpaged())
                .forEach(checkIn -> checkInByUser.put(checkIn.getAttendee().getId(), checkIn));

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Attendance");
            sheet.setColumnWidth(0, 18 * 256);
            sheet.setColumnWidth(1, 26 * 256);
            sheet.setColumnWidth(2, 12 * 256);
            sheet.setColumnWidth(3, 16 * 256);
            sheet.setColumnWidth(4, 20 * 256);

            Row header = sheet.createRow(0);
            String[] titles = {"姓名", "邮箱", "报名状态", "签到状态", "签到方式", "签到时间"};
            for (int i = 0; i < titles.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(titles[i]);
            }

            CellStyle datetimeStyle = workbook.createCellStyle();
            datetimeStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd hh:mm"));

            int rowIndex = 1;
            for (ActivityRegistration reg : registrations) {
                Row row = sheet.createRow(rowIndex++);
                ActivityCheckIn checkIn = checkInByUser.get(reg.getAttendee().getId());
                row.createCell(0).setCellValue(reg.getAttendee().getFullName());
                row.createCell(1).setCellValue(reg.getAttendee().getEmail());
                row.createCell(2).setCellValue(reg.getStatus().name());
                row.createCell(3).setCellValue(checkIn != null ? "已签到" : "未签到");
                row.createCell(4).setCellValue(checkIn != null ? checkIn.getMethod().name() : "-");
                Cell timeCell = row.createCell(5);
                if (checkIn != null) {
                    timeCell.setCellValue(java.util.Date.from(checkIn.getCheckedInAt()));
                    timeCell.setCellStyle(datetimeStyle);
                } else {
                    timeCell.setCellValue("-");
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("导出考勤报表失败，请稍后再试");
        }
    }

    private void validateCheckInToken(Activity activity, String token) {
        if (activity.getCheckInToken() == null || !activity.getCheckInToken().equals(token)) {
            throw new BusinessException("签到凭证无效，请刷新二维码");
        }
        Instant expiresAt = activity.getCheckInTokenExpiresAt();
        if (expiresAt != null && Instant.now().isAfter(expiresAt)) {
            throw new BusinessException("签到二维码已过期，请联系负责人刷新");
        }
    }

    private CheckInMethod resolveMethod(String method) {
        if (method == null || method.isBlank()) {
            return CheckInMethod.QR_CODE;
        }
        try {
            return CheckInMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return CheckInMethod.QR_CODE;
        }
    }

    private String buildCheckInPayload(Long activityId, String token, Instant expiresAt) {
        String base = frontendBaseUrl != null ? frontendBaseUrl.trim() : "";
        if (!base.isEmpty() && base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String path = "/check-in?activity=" + activityId + "&token=" + token;
        if (expiresAt != null) {
            path += "&exp=" + expiresAt.toEpochMilli();
        }
        if (base.isEmpty()) {
            return "campus-club://check-in?activity=" + activityId + "&token=" + token;
        }
        return base + path;
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
