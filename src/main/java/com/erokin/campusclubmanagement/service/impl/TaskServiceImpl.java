package com.erokin.campusclubmanagement.service.impl;

import com.erokin.campusclubmanagement.dto.task.ClubTaskRequest;
import com.erokin.campusclubmanagement.dto.task.ClubTaskResponse;
import com.erokin.campusclubmanagement.dto.task.TaskStatusUpdateRequest;
import com.erokin.campusclubmanagement.entity.Club;
import com.erokin.campusclubmanagement.entity.ClubMembership;
import com.erokin.campusclubmanagement.entity.ClubTask;
import com.erokin.campusclubmanagement.entity.ClubTaskAssignment;
import com.erokin.campusclubmanagement.entity.User;
import com.erokin.campusclubmanagement.enums.MembershipRole;
import com.erokin.campusclubmanagement.enums.MembershipStatus;
import com.erokin.campusclubmanagement.enums.Role;
import com.erokin.campusclubmanagement.enums.TaskStatus;
import com.erokin.campusclubmanagement.exception.BusinessException;
import com.erokin.campusclubmanagement.exception.ResourceNotFoundException;
import com.erokin.campusclubmanagement.repository.ClubMembershipRepository;
import com.erokin.campusclubmanagement.repository.ClubTaskAssignmentRepository;
import com.erokin.campusclubmanagement.repository.ClubTaskRepository;
import com.erokin.campusclubmanagement.repository.ClubRepository;
import com.erokin.campusclubmanagement.repository.UserRepository;
import com.erokin.campusclubmanagement.service.TaskService;
import com.erokin.campusclubmanagement.util.SecurityUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final ClubRepository clubRepository;
    private final ClubTaskRepository taskRepository;
    private final ClubTaskAssignmentRepository assignmentRepository;
    private final ClubMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    @Override
    public ClubTaskResponse createTask(Long clubId, ClubTaskRequest request) {
        Club club = findClub(clubId);
        User operator = getCurrentUser();
        ensureManager(operator, club);

        ClubTask task = new ClubTask();
        task.setClub(club);
        task.setCreator(operator);
        applyRequest(task, request);
        task.setStatus(TaskStatus.NOT_STARTED);
        ClubTask savedTask = taskRepository.save(task);

        if (request.getAssigneeIds() != null && !request.getAssigneeIds().isEmpty()) {
            attachAssignments(savedTask, request.getAssigneeIds(), operator);
        }

        return toResponse(savedTask, assignmentRepository.findByTask(savedTask));
    }

    @Override
    public ClubTaskResponse updateTask(Long taskId, ClubTaskRequest request) {
        ClubTask task = taskRepository
                .findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        User operator = getCurrentUser();
        ensureManager(operator, task.getClub());

        applyRequest(task, request);
        taskRepository.save(task);

        if (request.getAssigneeIds() != null) {
            reconcileAssignments(task, request.getAssigneeIds(), operator);
        }

        return toResponse(task, assignmentRepository.findByTask(task));
    }

    @Override
    public ClubTaskResponse updateAssignmentStatus(
            Long taskId, Long assignmentId, TaskStatusUpdateRequest request) {
        ClubTask task = taskRepository
                .findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        ClubTaskAssignment assignment = assignmentRepository
                .findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("任务指派不存在"));
        if (!assignment.getTask().getId().equals(taskId)) {
            throw new BusinessException("任务指派不匹配");
        }

        User user = getCurrentUser();
        boolean isManager;
        try {
            ensureManager(user, task.getClub());
            isManager = true;
        } catch (BusinessException ignored) {
            isManager = false;
        }

        if (!isManager && !assignment.getAssignee().getId().equals(user.getId())) {
            throw new BusinessException("没有权限更新该任务");
        }

        assignment.setStatus(request.getStatus());
        assignment.setRemark(request.getRemark());
        assignmentRepository.save(assignment);

        if (request.getStatus() == TaskStatus.COMPLETED) {
            task.setStatus(TaskStatus.COMPLETED);
        } else if (request.getStatus() == TaskStatus.IN_PROGRESS && task.getStatus() == TaskStatus.NOT_STARTED) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }
        taskRepository.save(task);

        return toResponse(task, assignmentRepository.findByTask(task));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClubTaskResponse> listTasks(Long clubId, Pageable pageable) {
        Club club = findClub(clubId);
        ensureManager(getCurrentUser(), club);
        return taskRepository.findByClub(club, pageable)
                .map(task -> toResponse(task, assignmentRepository.findByTask(task)));
    }

    @Override
    @Transactional(readOnly = true)
    public ClubTaskResponse getTask(Long taskId) {
        ClubTask task = taskRepository
                .findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在"));
        ensureManager(getCurrentUser(), task.getClub());
        return toResponse(task, assignmentRepository.findByTask(task));
    }

    private void applyRequest(ClubTask task, ClubTaskRequest request) {
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueAt(request.getDueAt());
    }

    private void attachAssignments(ClubTask task, List<Long> assigneeIds, User operator) {
        Set<Long> uniqueIds = new HashSet<>(assigneeIds);
        Map<Long, User> userMap = userRepository.findAllById(uniqueIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        uniqueIds.forEach(id -> {
            User assignee = userMap.get(id);
            if (assignee == null) {
                throw new BusinessException("成员不存在: " + id);
            }
            ClubMembership membership = membershipRepository
                    .findByClubAndMember(task.getClub(), assignee)
                    .orElseThrow(() -> new BusinessException(assignee.getFullName() + " 尚未加入社团"));
            if (membership.getStatus() != MembershipStatus.APPROVED) {
                throw new BusinessException(assignee.getFullName() + " 尚未正式加入社团");
            }
            ClubTaskAssignment assignment = new ClubTaskAssignment();
            assignment.setTask(task);
            assignment.setAssignee(assignee);
            assignmentRepository.save(assignment);
        });

        if (!uniqueIds.isEmpty()) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    private void reconcileAssignments(ClubTask task, List<Long> assigneeIds, User operator) {
        List<ClubTaskAssignment> existing = assignmentRepository.findByTask(task);
        Map<Long, ClubTaskAssignment> existingMap = existing.stream()
                .collect(Collectors.toMap(a -> a.getAssignee().getId(), a -> a));

        Set<Long> newIds = new HashSet<>(assigneeIds);

        // Remove assignments no longer present
        existing.stream()
                .filter(assignment -> !newIds.contains(assignment.getAssignee().getId()))
                .forEach(assignmentRepository::delete);

        // Add new assignments
        List<Long> toAdd = newIds.stream()
                .filter(id -> !existingMap.containsKey(id))
                .collect(Collectors.toList());
        if (!toAdd.isEmpty()) {
            attachAssignments(task, toAdd, operator);
        }

        if (newIds.isEmpty()) {
            task.setStatus(TaskStatus.NOT_STARTED);
        } else if (task.getStatus() == TaskStatus.NOT_STARTED) {
            task.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    private ClubTaskResponse toResponse(ClubTask task, List<ClubTaskAssignment> assignments) {
        return ClubTaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .dueAt(task.getDueAt())
                .status(task.getStatus())
                .creatorId(task.getCreator().getId())
                .creatorName(task.getCreator().getFullName())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .assignments(assignments.stream()
                        .map(assignment -> ClubTaskResponse.TaskAssignmentResponse.builder()
                                .assignmentId(assignment.getId())
                                .userId(assignment.getAssignee().getId())
                                .userName(assignment.getAssignee().getFullName())
                                .userEmail(assignment.getAssignee().getEmail())
                                .status(assignment.getStatus())
                                .remark(assignment.getRemark())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private Club findClub(Long id) {
        return clubRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("社团不存在"));
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
        membershipRepository
                .findByClubAndMember(club, user)
                .filter(membership ->
                        membership.getStatus() == MembershipStatus.APPROVED
                                && membership.getMembershipRole() == MembershipRole.LEADER)
                .orElseThrow(() -> new BusinessException("没有权限管理该社团任务"));
    }
}
