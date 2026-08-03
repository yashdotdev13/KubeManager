package com.kubemanager.user_service.service.Impl;

import com.kubemanager.user_service.dtos.request.UpdateUserProfileRequest;
import com.kubemanager.user_service.dtos.response.UserPreferenceResponse;
import com.kubemanager.user_service.dtos.response.UserProfileResponse;
import com.kubemanager.user_service.repository.UserPreferenceRepository;
import com.kubemanager.user_service.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;

    @Override
    public UserPreferenceResponse getUserPreference() {
        return null;
    }

    @Override
    public UserPreferenceResponse updatePreferences(UpdateUserProfileRequest request) {
        return null;
    }

    @Override
    public UserProfileResponse resetPreferences() {
        return null;
    }
}
