package com.erokin.campusclubmanagement.dto.video;

import com.erokin.campusclubmanagement.enums.PromoVideoStatus;
import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromoVideoResponse {
    private Long id;
    private Long clubId;
    private String playbackUrl;
    private String originalFileName;
    private PromoVideoStatus status;
    private Integer durationSeconds;
    private Long fileSizeBytes;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant subtitlesUpdatedAt;
    private List<SubtitleSegmentResponse> subtitles;
}
