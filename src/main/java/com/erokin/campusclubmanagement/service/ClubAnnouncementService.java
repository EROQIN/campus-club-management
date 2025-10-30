package com.erokin.campusclubmanagement.service;

import com.erokin.campusclubmanagement.dto.announcement.AnnouncementRequest;
import com.erokin.campusclubmanagement.dto.announcement.AnnouncementResponse;
import java.util.List;

public interface ClubAnnouncementService {

    List<AnnouncementResponse> listAnnouncements(Long clubId);

    AnnouncementResponse publish(Long clubId, AnnouncementRequest request);
}

