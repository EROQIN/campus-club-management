package com.erokin.campusclubmanagement.dto.collaboration;

import com.erokin.campusclubmanagement.enums.CollaborationStatus;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CollaborationProposalResponse {
    private final Long id;
    private final Long initiatorClubId;
    private final String initiatorClubName;
    private final Long initiatorUserId;
    private final String initiatorUserName;
    private final String title;
    private final String description;
    private final String collaborationType;
    private final String requiredResources;
    private final CollaborationStatus status;
    private final Instant createdAt;
    private final List<ProposalResponseDto> responses;

    @Getter
    @Builder
    public static class ProposalResponseDto {
        private final Long id;
        private final Long responderClubId;
        private final String responderClubName;
        private final Long responderUserId;
        private final String responderUserName;
        private final String message;
        private final String status;
        private final Instant createdAt;
    }
}
