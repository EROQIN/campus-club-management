package com.erokin.campusclubmanagement.service;

import com.erokin.campusclubmanagement.dto.club.ClubRequest;
import com.erokin.campusclubmanagement.dto.club.ClubResponse;
import com.erokin.campusclubmanagement.dto.club.ClubSummaryResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClubService {

    ClubResponse createClub(ClubRequest request);

    ClubResponse updateClub(Long clubId, ClubRequest request);

    void deleteClub(Long clubId);

    ClubResponse getClub(Long clubId);

    Page<ClubSummaryResponse> searchClubs(
            String keywords, String category, List<String> tags, Pageable pageable);

    Page<ClubSummaryResponse> recommendClubs(Pageable pageable);

    List<ClubSummaryResponse> listMyClubs();
}
