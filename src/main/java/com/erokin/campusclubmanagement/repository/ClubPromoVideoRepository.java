package com.erokin.campusclubmanagement.repository;

import com.erokin.campusclubmanagement.entity.ClubPromoVideo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubPromoVideoRepository extends JpaRepository<ClubPromoVideo, Long> {

    Optional<ClubPromoVideo> findByClubId(Long clubId);
}
