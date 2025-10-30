package com.erokin.campusclubmanagement.service;

import com.erokin.campusclubmanagement.dto.video.PromoVideoResponse;
import com.erokin.campusclubmanagement.dto.video.UpdateSubtitlesRequest;
import org.springframework.web.multipart.MultipartFile;

public interface ClubPromoVideoService {

    PromoVideoResponse uploadPromoVideo(Long clubId, MultipartFile file, Integer durationSeconds);

    PromoVideoResponse generateSubtitles(Long clubId);

    PromoVideoResponse updateSubtitles(Long clubId, UpdateSubtitlesRequest request);

    PromoVideoResponse getPromoVideo(Long clubId);
}
