package com.erokin.campusclubmanagement.dto.club;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClubSummaryResponse {
    private Long id;
    private String name;
    private String description;
    private String logoUrl;
    private String category;
    private int memberCount;
    private int activityCount;
    private List<String> tags;
    private int recommendationScore;
}
