package com.erokin.campusclubmanagement.dto.resource;

import com.erokin.campusclubmanagement.enums.ResourceApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceApplicationDecisionRequest {

    @NotNull
    private Boolean approve;

    @Size(max = 300)
    private String replyMessage;

    public ResourceApplicationStatus resolveStatus() {
        return Boolean.TRUE.equals(approve)
                ? ResourceApplicationStatus.APPROVED
                : ResourceApplicationStatus.REJECTED;
    }
}

