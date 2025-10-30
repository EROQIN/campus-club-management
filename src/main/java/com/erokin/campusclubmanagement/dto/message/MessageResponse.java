package com.erokin.campusclubmanagement.dto.message;

import com.erokin.campusclubmanagement.enums.MessageType;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageResponse {
    private Long id;
    private MessageType type;
    private String title;
    private String content;
    private boolean read;
    private Instant createdAt;
    private Instant readAt;
    private String referenceType;
    private Long referenceId;
}

