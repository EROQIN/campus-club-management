package com.erokin.campusclubmanagement.dto.announcement;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnouncementResponse {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private String authorName;
    private Instant createdAt;
}

