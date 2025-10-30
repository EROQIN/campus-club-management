package com.erokin.campusclubmanagement.repository;

import com.erokin.campusclubmanagement.entity.ResourceApplication;
import com.erokin.campusclubmanagement.entity.SharedResource;
import com.erokin.campusclubmanagement.entity.User;
import com.erokin.campusclubmanagement.enums.ResourceApplicationStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceApplicationRepository extends JpaRepository<ResourceApplication, Long> {

    List<ResourceApplication> findByApplicant(User applicant);

    List<ResourceApplication> findByResource(SharedResource resource);

    long countByResourceAndStatus(SharedResource resource, ResourceApplicationStatus status);

    boolean existsByResourceAndApplicantAndRequestedFromAndRequestedUntil(
            SharedResource resource, User applicant, Instant requestedFrom, Instant requestedUntil);
}

