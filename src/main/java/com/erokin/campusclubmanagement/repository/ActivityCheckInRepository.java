package com.erokin.campusclubmanagement.repository;

import com.erokin.campusclubmanagement.entity.Activity;
import com.erokin.campusclubmanagement.entity.ActivityCheckIn;
import com.erokin.campusclubmanagement.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityCheckInRepository extends JpaRepository<ActivityCheckIn, Long> {

    boolean existsByActivityAndAttendee(Activity activity, User attendee);

    Optional<ActivityCheckIn> findByActivityAndAttendee(Activity activity, User attendee);

    Page<ActivityCheckIn> findByActivity(Activity activity, Pageable pageable);
}
