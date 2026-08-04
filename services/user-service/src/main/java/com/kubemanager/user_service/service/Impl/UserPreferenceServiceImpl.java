package com.kubemanager.user_service.service.Impl;

import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import com.kubemanager.user_service.auth.UserContext;
import com.kubemanager.user_service.auth.UserContextHolder;
import com.kubemanager.user_service.dtos.request.UpdateUserPreferenceRequest;
import com.kubemanager.user_service.dtos.response.UserPreferenceResponse;
import com.kubemanager.user_service.entity.UserPreference;
import com.kubemanager.user_service.mapper.UserPreferenceMapper;
import com.kubemanager.user_service.repository.UserPreferenceRepository;
import com.kubemanager.user_service.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;
    private final UserPreferenceMapper userPreferenceMapper;



    @Override
    @Transactional(readOnly = true)
    public UserPreferenceResponse getUserPreference() {


        UserContext userContext = UserContextHolder.getRequiredContext();
        UUID userId = userContext.getUserId();

        log.info("Fetching preferences for user '{}' ({})",userContext.getUsername(),userId);

        UserPreference preference = userPreferenceRepository.findByUserId(userId)
                .orElseThrow(()->{
                    log.warn("Preferences not found for user '{}' ({})",
                            userContext.getUserId(),userId);

                    return new ResourceNotFoundException(
                            ErrorCode.USER_PREFERENCE_NOT_FOUND,
                            "User preferences not found"
                    );

                });

        log.info("Preferences fetched successfully for user '{}' ({})",
                userContext.getUsername(), userId);

        return userPreferenceMapper.toResponse(preference);

    }

    @Override
    public UserPreferenceResponse updatePreferences(
            UpdateUserPreferenceRequest request
    ) {

        if (request == null) {
            throw new BadRequestException(
                    ErrorCode.INVALID_USER_PREFERENCE,
                    "Request body cannot be null."
            );
        }

        UserContext userContext = UserContextHolder.getRequiredContext();
        UUID userId = userContext.getUserId();

        log.info(
                "Updating preferences for user '{}' ({}).",
                userContext.getUsername(),
                userId
        );

        UserPreference preference = userPreferenceRepository
                .findByUserId(userId)
                .orElseThrow(() -> {

                    log.warn(
                            "Preferences not found for user '{}' ({}).",
                            userContext.getUsername(),
                            userId
                    );

                    return new ResourceNotFoundException(
                            ErrorCode.USER_PREFERENCE_NOT_FOUND,
                            "User preferences not found."
                    );
                });

        userPreferenceMapper.updateEntity(
                preference,
                request
        );

        UserPreference updatedPreference =
                userPreferenceRepository.save(preference);

        log.info(
                "Preferences updated successfully for user '{}' ({}).",
                userContext.getUsername(),
                userId
        );

        return userPreferenceMapper.toResponse(updatedPreference);
    }

    @Override
    public UserPreferenceResponse resetPreferences() {

        UserContext userContext = UserContextHolder.getRequiredContext();
        UUID userId = userContext.getUserId();

        log.info(
                "Resetting preferences for user '{}' ({}).",
                userContext.getUsername(),
                userId
        );

        UserPreference preference = userPreferenceRepository
                .findByUserId(userId)
                .orElseThrow(() -> {

                    log.warn(
                            "Preferences not found for user '{}' ({}).",
                            userContext.getUsername(),
                            userId
                    );

                    return new ResourceNotFoundException(
                            ErrorCode.USER_PREFERENCE_NOT_FOUND,
                            "User preferences not found."
                    );
                });

        userPreferenceMapper.resetToDefault(preference);

        UserPreference updatedPreference =
                userPreferenceRepository.save(preference);

        log.info(
                "Preferences reset successfully for user '{}' ({}).",
                userContext.getUsername(),
                userId
        );

        return userPreferenceMapper.toResponse(updatedPreference);
    }
}
