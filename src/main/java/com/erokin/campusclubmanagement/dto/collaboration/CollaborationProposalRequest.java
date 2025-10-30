package com.erokin.campusclubmanagement.dto.collaboration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollaborationProposalRequest {

    @NotBlank(message = "请输入提案标题")
    @Size(max = 150)
    private String title;

    @NotBlank(message = "请输入提案内容")
    @Size(max = 1000)
    private String description;

    @Size(max = 100)
    private String collaborationType;

    @Size(max = 300)
    private String requiredResources;
}
