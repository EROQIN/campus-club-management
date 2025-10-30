package com.erokin.campusclubmanagement.service;

import com.erokin.campusclubmanagement.dto.task.ClubTaskRequest;
import com.erokin.campusclubmanagement.dto.task.ClubTaskResponse;
import com.erokin.campusclubmanagement.dto.task.TaskStatusUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    ClubTaskResponse createTask(Long clubId, ClubTaskRequest request);

    ClubTaskResponse updateTask(Long taskId, ClubTaskRequest request);

    ClubTaskResponse updateAssignmentStatus(Long taskId, Long assignmentId, TaskStatusUpdateRequest request);

    Page<ClubTaskResponse> listTasks(Long clubId, Pageable pageable);

    ClubTaskResponse getTask(Long taskId);
}
