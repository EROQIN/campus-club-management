package com.erokin.campusclubmanagement.dto.task;

import com.erokin.campusclubmanagement.enums.TaskStatus;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClubTaskResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final Instant dueAt;
    private final TaskStatus status;
    private final Long creatorId;
    private final String creatorName;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final List<TaskAssignmentResponse> assignments;

    @Getter
    @Builder
    public static class TaskAssignmentResponse {
        private final Long assignmentId;
        private final Long userId;
        private final String userName;
        private final String userEmail;
        private final TaskStatus status;
        private final String remark;
    }
}
