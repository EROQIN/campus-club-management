package com.erokin.campusclubmanagement.repository;

import com.erokin.campusclubmanagement.entity.SharedResource;
import com.erokin.campusclubmanagement.entity.Club;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SharedResourceRepository extends JpaRepository<SharedResource, Long> {

    List<SharedResource> findByClub(Club club);

    List<SharedResource> findByResourceTypeIgnoreCase(String resourceType);
}

