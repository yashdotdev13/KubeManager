package com.kubemanager.user_service.service.Impl;


import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ConflictException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import com.kubemanager.user_service.auth.UserContext;
import com.kubemanager.user_service.auth.UserContextHolder;
import com.kubemanager.user_service.dtos.request.CreateUserProfileRequest;
import com.kubemanager.user_service.dtos.request.UpdateUserProfileRequest;
import com.kubemanager.user_service.dtos.response.UserProfileResponse;
import com.kubemanager.user_service.entity.UserPreference;
import com.kubemanager.user_service.entity.UserProfile;
import com.kubemanager.user_service.mapper.UserPreferenceMapper;
import com.kubemanager.user_service.mapper.UserProfileMapper;
import com.kubemanager.user_service.repository.UserPreferenceRepository;
import com.kubemanager.user_service.repository.UserProfileRepository;
import com.kubemanager.user_service.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;
    private final UserPreferenceMapper userPreferenceMapper;
    private final UserPreferenceRepository userPreferenceRepository;



    @Override
    public UserProfileResponse createProfile(
            CreateUserProfileRequest request
    ) {

        if (request == null) {
            throw new BadRequestException(
                    ErrorCode.INVALID_USER_PROFILE,
                    "Request body cannot be null."
            );
        }

        UserContext userContext = UserContextHolder.getRequiredContext();
        UUID userId = userContext.getUserId();

        log.info(
                "Creating profile for user '{}' ({})",
                userContext.getUsername(),
                userId
        );

        if (userProfileRepository.existsByUserId(userId)) {

            log.warn(
                    "Profile already exists for user '{}' ({})",
                    userContext.getUsername(),
                    userId
            );

            throw new ConflictException(
                    ErrorCode.USER_PROFILE_ALREADY_EXISTS,
                    "User profile already exists."
            );
        }

        UserProfile profile = userProfileMapper.toEntity(request);
        profile.setUserId(userId);

        UserProfile savedProfile =
                userProfileRepository.save(profile);

        UserPreference preference =
                userPreferenceMapper.toEntity(userId);

        userPreferenceRepository.save(preference);

        log.info(
                "Default preferences created for user '{}' ({}).",
                userContext.getUsername(),
                userId
        );

        log.info(
                "Profile created successfully for user '{}' ({})",
                userContext.getUsername(),
                userId
        );
        return userProfileMapper.toResponse(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {

        UserContext userContext = UserContextHolder.getRequiredContext();
        UUID userId = userContext.getUserId();

        log.info(
                "Fetching profile for the user '{}' ({}).",
                userContext.getUsername(),
                userId
        );

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> {

                    log.warn(
                            "Profile not found for user '{}' ({})",
                            userContext.getUsername(),
                            userId
                    );

                    return new ResourceNotFoundException(
                            ErrorCode.USER_PROFILE_NOT_FOUND,
                            "Profile not found for user '" + userContext.getUsername() + "'."
                    );
                });

        log.info(
                "Profile fetched successfully for user '{}' ({}).",
                userContext.getUsername(),
                userId
        );

        return userProfileMapper.toResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfileByUserId(
            UUID userId
    ) {

        log.info("Fetching profile for user '{}'.", userId);

        UserProfile profile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> {

                    log.warn(
                            "Profile not found for user '{}'.",
                            userId
                    );

                    return new ResourceNotFoundException(
                            ErrorCode.USER_PROFILE_NOT_FOUND,
                            "User profile not found."
                    );
                });

        log.info(
                "Profile fetched successfully for user '{}'.",
                userId
        );

        return userProfileMapper.toResponse(profile);
    }

    @Override
    public UserProfileResponse updateProfile(
            UpdateUserProfileRequest request
    ) {

        if (request == null) {
            throw new BadRequestException(
                    ErrorCode.INVALID_USER_PROFILE,
                    "Request body cannot be null."
            );
        }

        UserContext userContext = UserContextHolder.getRequiredContext();
        UUID userId = userContext.getUserId();

        log.info(
                "Updating profile for user '{}' ({}).",
                userContext.getUsername(),
                userId
        );

        UserProfile profile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> {

                    log.warn(
                            "Profile not found for user '{}' ({}).",
                            userContext.getUsername(),
                            userId
                    );

                    return new ResourceNotFoundException(
                            ErrorCode.USER_PROFILE_NOT_FOUND,
                            "User profile not found."
                    );
                });

        userProfileMapper.updateEntity(
                profile,
                request
        );

        UserProfile updatedProfile =
                userProfileRepository.save(profile);

        log.info(
                "Profile updated successfully for user '{}' ({}).",
                userContext.getUsername(),
                userId
        );

        return userProfileMapper.toResponse(updatedProfile);
    }

    @Override
    public void deleteProfile() {

        UserContext userContext = UserContextHolder.getRequiredContext();
        UUID userId = userContext.getUserId();

        log.info(
                "Deleting profile for user '{}' ({}).",
                userContext.getUsername(),
                userId
        );

        UserProfile profile = userProfileRepository
                .findByUserId(userId)
                .orElseThrow(() -> {

                    log.warn(
                            "Profile not found for user '{}' ({}).",
                            userContext.getUsername(),
                            userId
                    );

                    return new ResourceNotFoundException(
                            ErrorCode.USER_PROFILE_NOT_FOUND,
                            "User profile not found."
                    );
                });

        userProfileRepository.delete(profile);

        log.info(
                "Profile deleted successfully for user '{}' ({}).",
                userContext.getUsername(),
                userId
        );
    }
}
