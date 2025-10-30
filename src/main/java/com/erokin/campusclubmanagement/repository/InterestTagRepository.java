package com.erokin.campusclubmanagement.repository;

import com.erokin.campusclubmanagement.entity.InterestTag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestTagRepository extends JpaRepository<InterestTag, Long> {

    Optional<InterestTag> findByNameIgnoreCase(String name);
}

