package com.erokin.campusclubmanagement.repository;

import com.erokin.campusclubmanagement.entity.Activity;
import com.erokin.campusclubmanagement.entity.ActivityRegistration;
import com.erokin.campusclubmanagement.enums.ActivityRegistrationStatus;
import com.erokin.campusclubmanagement.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRegistrationRepository extends JpaRepository<ActivityRegistration, Long> {

    Optional<ActivityRegistration> findByActivityAndAttendee(Activity activity, User attendee);

    List<ActivityRegistration> findByActivity(Activity activity);

    Page<ActivityRegistration> findByActivity(Activity activity, Pageable pageable);

    List<ActivityRegistration> findByAttendee(User attendee);

    long countByActivity(Activity activity);

    long countByStatusAndCreatedAtAfter(ActivityRegistrationStatus status, Instant createdAt);
}
