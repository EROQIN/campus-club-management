package com.erokin.campusclubmanagement.repository;

import com.erokin.campusclubmanagement.entity.ClubTask;
import com.erokin.campusclubmanagement.entity.ClubTaskAssignment;
import com.erokin.campusclubmanagement.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubTaskAssignmentRepository extends JpaRepository<ClubTaskAssignment, Long> {

    List<ClubTaskAssignment> findByTask(ClubTask task);

    Optional<ClubTaskAssignment> findByTaskAndAssignee(ClubTask task, User assignee);
}
