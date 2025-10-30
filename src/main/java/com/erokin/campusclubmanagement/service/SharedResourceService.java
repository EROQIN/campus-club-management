package com.erokin.campusclubmanagement.service;

import com.erokin.campusclubmanagement.dto.resource.SharedResourceRequest;
import com.erokin.campusclubmanagement.dto.resource.SharedResourceResponse;
import java.util.List;

public interface SharedResourceService {

    SharedResourceResponse create(Long clubId, SharedResourceRequest request);

    SharedResourceResponse update(Long resourceId, SharedResourceRequest request);

    void delete(Long resourceId);

    List<SharedResourceResponse> listAll();

    List<SharedResourceResponse> listByClub(Long clubId);
}

