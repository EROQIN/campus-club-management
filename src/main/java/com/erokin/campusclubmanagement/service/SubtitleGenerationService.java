package com.erokin.campusclubmanagement.service;

import java.util.List;

import com.erokin.campusclubmanagement.service.dto.GeneratedSubtitleSegment;

public interface SubtitleGenerationService {

    /**
     * Generates subtitle segments from the given media resource URL.
     *
     * @param mediaUrl accessible URL to the media file (audio/video) to transcribe
     * @return list of generated segments in chronological order
     */
    List<GeneratedSubtitleSegment> generateSubtitles(String mediaUrl);
}
