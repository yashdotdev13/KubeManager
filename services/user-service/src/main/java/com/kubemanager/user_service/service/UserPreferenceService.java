package com.kubemanager.user_service.service;

import com.kubemanager.user_service.dtos.request.UpdateUserPreferenceRequest;
import com.kubemanager.user_service.dtos.request.UpdateUserProfileRequest;
import com.kubemanager.user_service.dtos.response.UserPreferenceResponse;
import com.kubemanager.user_service.dtos.response.UserProfileResponse;

public interface UserPreferenceService {

    UserPreferenceResponse getUserPreference();

    UserPreferenceResponse updatePreferences(
            UpdateUserPreferenceRequest request
    );

    UserPreferenceResponse resetPreferences();
}
