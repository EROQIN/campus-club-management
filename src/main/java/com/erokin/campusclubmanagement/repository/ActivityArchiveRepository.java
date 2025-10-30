package com.erokin.campusclubmanagement.repository;

import com.erokin.campusclubmanagement.entity.ActivityArchive;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityArchiveRepository extends JpaRepository<ActivityArchive, Long> {

    Optional<ActivityArchive> findByActivityId(Long activityId);

    @Query(
            "select aa from ActivityArchive aa "
                    + "join aa.activity act "
                    + "where act.club.id = :clubId "
                    + "and (:keywords is null "
                    + "     or lower(act.title) like lower(concat('%', :keywords, '%')) "
                    + "     or lower(aa.summary) like lower(concat('%', :keywords, '%'))) "
                    + "and (:start is null or aa.archivedAt >= :start) "
                    + "and (:end is null or aa.archivedAt <= :end)")
    Page<ActivityArchive> searchByClub(
            @Param("clubId") Long clubId,
            @Param("keywords") String keywords,
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable);
}
