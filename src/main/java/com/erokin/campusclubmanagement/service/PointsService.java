package com.erokin.campusclubmanagement.service;

import com.erokin.campusclubmanagement.dto.points.PointLeaderboardEntry;
import com.erokin.campusclubmanagement.dto.points.PointRecordRequest;
import com.erokin.campusclubmanagement.dto.points.PointRecordResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PointsService {

    PointRecordResponse addRecord(Long clubId, PointRecordRequest request);

    Page<PointRecordResponse> listRecords(Long clubId, Pageable pageable);

    List<PointLeaderboardEntry> leaderboard(Long clubId, int limit);
}
