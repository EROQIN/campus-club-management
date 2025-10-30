package com.erokin.campusclubmanagement.repository;

import com.erokin.campusclubmanagement.entity.ClubPromoSubtitle;
import com.erokin.campusclubmanagement.entity.ClubPromoVideo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubPromoSubtitleRepository extends JpaRepository<ClubPromoSubtitle, Long> {

    List<ClubPromoSubtitle> findByVideoOrderBySequenceAsc(ClubPromoVideo video);
}
