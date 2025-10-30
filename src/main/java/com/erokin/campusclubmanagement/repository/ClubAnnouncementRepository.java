package com.erokin.campusclubmanagement.repository;

import com.erokin.campusclubmanagement.entity.Club;
import com.erokin.campusclubmanagement.entity.ClubAnnouncement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubAnnouncementRepository extends JpaRepository<ClubAnnouncement, Long> {

    List<ClubAnnouncement> findByClubOrderByCreatedAtDesc(Club club);
}

