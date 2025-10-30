package com.erokin.campusclubmanagement.service;

import com.erokin.campusclubmanagement.dto.user.UpdateProfileRequest;
import com.erokin.campusclubmanagement.dto.user.UserProfileResponse;

public interface UserService {

    UserProfileResponse getCurrentUserProfile();

    UserProfileResponse updateCurrentUserProfile(UpdateProfileRequest request);
}

