package com.erokin.campusclubmanagement.repository;

import com.erokin.campusclubmanagement.entity.Club;
import com.erokin.campusclubmanagement.entity.ClubPointRecord;
import com.erokin.campusclubmanagement.entity.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClubPointRecordRepository extends JpaRepository<ClubPointRecord, Long> {

    Page<ClubPointRecord> findByClub(Club club, Pageable pageable);

    List<ClubPointRecord> findByClubAndMember(Club club, User member);

    @Query("select r.member as member, sum(r.points) as total from ClubPointRecord r where r.club = :club group by r.member order by total desc")
    List<Object[]> summarizeByMember(Club club);
}
