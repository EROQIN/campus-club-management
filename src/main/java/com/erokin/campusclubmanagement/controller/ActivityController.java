package com.erokin.campusclubmanagement.controller;

import com.erokin.campusclubmanagement.dto.activity.ActivityArchiveRequest;
import com.erokin.campusclubmanagement.dto.activity.ActivityArchiveResponse;
import com.erokin.campusclubmanagement.dto.activity.ActivityArchiveSummaryResponse;
import com.erokin.campusclubmanagement.dto.activity.ActivityCheckInRequest;
import com.erokin.campusclubmanagement.dto.activity.ActivityCheckInResponse;
import com.erokin.campusclubmanagement.dto.activity.ActivityRegistrationRequest;
import com.erokin.campusclubmanagement.dto.activity.ActivityRegistrationResponse;
import com.erokin.campusclubmanagement.dto.activity.ActivityRequest;
import com.erokin.campusclubmanagement.dto.activity.ActivityResponse;
import com.erokin.campusclubmanagement.dto.activity.ActivitySummaryResponse;
import com.erokin.campusclubmanagement.dto.activity.CheckInQrResponse;
import com.erokin.campusclubmanagement.dto.activity.ManualCheckInRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.erokin.campusclubmanagement.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping("/api")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping("/clubs/{clubId}/activities")
    public ResponseEntity<ActivityResponse> create(
            @PathVariable Long clubId, @Valid @RequestBody ActivityRequest request) {
        return ResponseEntity.ok(activityService.createActivity(clubId, request));
    }

    @PutMapping("/activities/{id}")
    public ResponseEntity<ActivityResponse> update(
            @PathVariable Long id, @Valid @RequestBody ActivityRequest request) {
        return ResponseEntity.ok(activityService.updateActivity(id, request));
    }

    @DeleteMapping("/activities/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/activities/{id}")
    public ResponseEntity<ActivityResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getActivity(id));
    }

    @GetMapping("/clubs/{clubId}/activities")
    public ResponseEntity<Page<ActivitySummaryResponse>> clubActivities(
            @PathVariable Long clubId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 50));
        return ResponseEntity.ok(activityService.listClubActivities(clubId, pageable));
    }

    @GetMapping("/activities/upcoming")
    public ResponseEntity<Page<ActivitySummaryResponse>> upcoming(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 50));
        return ResponseEntity.ok(activityService.listUpcomingActivities(pageable));
    }

    @PostMapping("/activities/{id}/register")
    public ResponseEntity<ActivityRegistrationResponse> register(
            @PathVariable Long id, @Valid @RequestBody ActivityRegistrationRequest request) {
        return ResponseEntity.ok(activityService.registerForActivity(id, request));
    }

    @PostMapping("/registrations/{id}/decision")
    public ResponseEntity<ActivityRegistrationResponse> decide(
            @PathVariable Long id, @RequestParam("approve") boolean approve) {
        return ResponseEntity.ok(activityService.reviewRegistration(id, approve));
    }

    @GetMapping("/activities/{id}/registrations")
    public ResponseEntity<Page<ActivityRegistrationResponse>> registrations(
            @PathVariable Long id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 100));
        return ResponseEntity.ok(activityService.listRegistrationsForActivity(id, pageable));
    }

    @PostMapping("/activities/{id}/check-in/qr")
    public ResponseEntity<CheckInQrResponse> generateQr(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.generateCheckInQr(id));
    }

    @PostMapping("/activities/{id}/check-in")
    public ResponseEntity<ActivityCheckInResponse> checkIn(
            @PathVariable Long id, @Valid @RequestBody ActivityCheckInRequest request) {
        return ResponseEntity.ok(activityService.checkIn(id, request));
    }

    @GetMapping("/activities/{id}/check-ins")
    public ResponseEntity<Page<ActivityCheckInResponse>> checkInRecords(
            @PathVariable Long id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 200));
        return ResponseEntity.ok(activityService.listCheckIns(id, pageable));
    }

    @PostMapping("/activities/{id}/check-in/manual")
    public ResponseEntity<Page<ActivityCheckInResponse>> manualCheckIn(
            @PathVariable Long id,
            @Valid @RequestBody ManualCheckInRequest request,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 200));
        return ResponseEntity.ok(activityService.manualCheckIn(id, request, pageable));
    }

    @GetMapping("/activities/{id}/attendance/export")
    public ResponseEntity<ByteArrayResource> exportAttendance(@PathVariable Long id) {
        byte[] data = activityService.exportAttendance(id);
        ByteArrayResource resource = new ByteArrayResource(data);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData(
                "attachment", "activity-attendance-" + id + ".xlsx");
        headers.setContentLength(data.length);
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @PostMapping("/activities/{id}/archive")
    public ResponseEntity<ActivityArchiveResponse> archiveActivity(
            @PathVariable Long id, @Valid @RequestBody ActivityArchiveRequest request) {
        return ResponseEntity.ok(activityService.archiveActivity(id, request));
    }

    @GetMapping("/activities/{id}/archive")
    public ResponseEntity<ActivityArchiveResponse> getArchive(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getActivityArchive(id));
    }

    @GetMapping("/activities/{id}/archive/pdf")
    public ResponseEntity<ByteArrayResource> exportArchivePdf(@PathVariable Long id) {
        byte[] pdf = activityService.exportActivityArchivePdf(id);
        ByteArrayResource resource = new ByteArrayResource(pdf);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentLength(pdf.length);
        headers.setContentDispositionFormData("attachment", "activity-archive-" + id + ".pdf");
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @GetMapping("/clubs/{clubId}/archives")
    public ResponseEntity<Page<ActivityArchiveSummaryResponse>> listArchives(
            @PathVariable Long clubId,
            @RequestParam(value = "keywords", required = false) String keywords,
            @RequestParam(value = "start", required = false) String start,
            @RequestParam(value = "end", required = false) String end,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 50));
        Instant startInstant = parseDate(start, true);
        Instant endInstant = parseDate(end, false);
        return ResponseEntity.ok(
                activityService.listClubActivityArchives(clubId, keywords, startInstant, endInstant, pageable));
    }

    private Instant parseDate(String value, boolean startOfDay) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        LocalDate date = LocalDate.parse(value.trim());
        if (startOfDay) {
            return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        }
        return date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().minusSeconds(1);
    }
}
