package com.kubemanager.user_service.service;

import com.kubemanager.user_service.dtos.request.CreateUserProfileRequest;
import com.kubemanager.user_service.dtos.request.UpdateUserProfileRequest;
import com.kubemanager.user_service.dtos.response.UserProfileResponse;

import java.util.UUID;

public interface UserProfileService {

    UserProfileResponse createProfile(CreateUserProfileRequest request);

    UserProfileResponse getCurrentUserProfile();

    UserProfileResponse getProfileByUserId(UUID userId);

    UserProfileResponse updateProfile(UpdateUserProfileRequest request);

    void deleteProfile();
}
