package com.erokin.campusclubmanagement.repository;

import com.erokin.campusclubmanagement.entity.Activity;
import com.erokin.campusclubmanagement.entity.Club;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    Page<Activity> findByClub(Club club, Pageable pageable);

    Page<Activity> findByStartTimeAfter(Instant startTime, Pageable pageable);

    List<Activity> findTop5ByClubOrderByStartTimeDesc(Club club);

    long countByClubAndStartTimeAfter(Club club, Instant startTime);

    long countByStartTimeAfter(Instant startTime);
}
