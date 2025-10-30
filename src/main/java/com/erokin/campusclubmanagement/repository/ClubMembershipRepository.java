package com.erokin.campusclubmanagement.repository;

import com.erokin.campusclubmanagement.entity.Club;
import com.erokin.campusclubmanagement.entity.ClubMembership;
import com.erokin.campusclubmanagement.entity.User;
import com.erokin.campusclubmanagement.enums.MembershipStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubMembershipRepository extends JpaRepository<ClubMembership, Long> {

    Optional<ClubMembership> findByClubAndMember(Club club, User member);

    List<ClubMembership> findByClubAndStatus(Club club, MembershipStatus status);

    List<ClubMembership> findByMember(User member);

    long countByClubAndStatus(Club club, MembershipStatus status);

    long countByStatus(MembershipStatus status);
}
