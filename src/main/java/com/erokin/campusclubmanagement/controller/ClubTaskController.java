package com.erokin.campusclubmanagement.controller;

import com.erokin.campusclubmanagement.dto.task.ClubTaskRequest;
import com.erokin.campusclubmanagement.dto.task.ClubTaskResponse;
import com.erokin.campusclubmanagement.dto.task.TaskStatusUpdateRequest;
import com.erokin.campusclubmanagement.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clubs/{clubId}/tasks")
public class ClubTaskController {

    private final TaskService taskService;

    public ClubTaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<ClubTaskResponse> create(
            @PathVariable Long clubId, @Valid @RequestBody ClubTaskRequest request) {
        return ResponseEntity.ok(taskService.createTask(clubId, request));
    }

    @GetMapping
    public ResponseEntity<Page<ClubTaskResponse>> list(
            @PathVariable Long clubId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(size, 100));
        return ResponseEntity.ok(taskService.listTasks(clubId, pageable));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<ClubTaskResponse> detail(
            @PathVariable Long clubId, @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTask(taskId));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<ClubTaskResponse> update(
            @PathVariable Long clubId,
            @PathVariable Long taskId,
            @Valid @RequestBody ClubTaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(taskId, request));
    }

    @PutMapping("/{taskId}/assignments/{assignmentId}")
    public ResponseEntity<ClubTaskResponse> updateAssignmentStatus(
            @PathVariable Long clubId,
            @PathVariable Long taskId,
            @PathVariable Long assignmentId,
            @Valid @RequestBody TaskStatusUpdateRequest request) {
        return ResponseEntity.ok(taskService.updateAssignmentStatus(taskId, assignmentId, request));
    }
}
