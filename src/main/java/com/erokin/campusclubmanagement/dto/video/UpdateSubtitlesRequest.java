package com.erokin.campusclubmanagement.dto.video;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSubtitlesRequest {

    @Valid
    private List<SubtitleEditRequest> subtitles = new ArrayList<>();
}
