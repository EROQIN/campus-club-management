package com.erokin.campusclubmanagement.service;

import com.erokin.campusclubmanagement.dto.resource.ResourceApplicationDecisionRequest;
import com.erokin.campusclubmanagement.dto.resource.ResourceApplicationRequest;
import com.erokin.campusclubmanagement.dto.resource.ResourceApplicationResponse;
import java.util.List;

public interface ResourceApplicationService {

    ResourceApplicationResponse apply(Long resourceId, ResourceApplicationRequest request);

    ResourceApplicationResponse decide(Long applicationId, ResourceApplicationDecisionRequest request);

    void cancel(Long applicationId);

    List<ResourceApplicationResponse> listMyApplications();

    List<ResourceApplicationResponse> listApplicationsForResource(Long resourceId);
}

