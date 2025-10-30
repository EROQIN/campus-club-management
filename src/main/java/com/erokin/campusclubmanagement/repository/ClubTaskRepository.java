package com.erokin.campusclubmanagement.repository;

import com.erokin.campusclubmanagement.entity.Club;
import com.erokin.campusclubmanagement.entity.ClubTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubTaskRepository extends JpaRepository<ClubTask, Long> {

    Page<ClubTask> findByClub(Club club, Pageable pageable);
}
