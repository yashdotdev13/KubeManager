package com.kubemanager.user_service.service;


import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ConflictException;
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
import com.kubemanager.user_service.service.Impl.UserProfileServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserProfileMapper userProfileMapper;

    @Mock
    private UserPreferenceMapper userPreferenceMapper;

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    private UUID userId;
    private UserProfile profile;
    private UserPreference preference;
    private UserProfileResponse response;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        UserContextHolder.set(
                UserContext.builder()
                        .userId(userId)
                        .username("test-user")
                        .email("test@example.com")
                        .build()
        );

        profile = UserProfile.builder()
                .userId(userId)
                .build();

        preference = UserPreference.builder()
                .userId(userId)
                .build();

        response = UserProfileResponse.builder()
                .userId(userId)
                .build();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void createProfile_shouldCreateProfileAndDefaultPreferences() {
        CreateUserProfileRequest request =
                CreateUserProfileRequest.builder()
                        .firstName("Test")
                        .lastName("User")
                        .displayName("Test User")
                        .build();

        when(userProfileRepository.existsByUserId(userId))
                .thenReturn(false);
        when(userProfileMapper.toEntity(request))
                .thenReturn(profile);
        when(userProfileRepository.save(profile))
                .thenReturn(profile);
        when(userPreferenceMapper.toEntity(userId))
                .thenReturn(preference);
        when(userProfileMapper.toResponse(profile))
                .thenReturn(response);

        UserProfileResponse result =
                userProfileService.createProfile(request);

        assertSame(response, result);
        assertEquals(userId, profile.getUserId());

        verify(userProfileRepository).existsByUserId(userId);
        verify(userProfileRepository).save(profile);
        verify(userPreferenceMapper).toEntity(userId);
        verify(userPreferenceRepository).save(preference);
        verify(userProfileMapper).toResponse(profile);
    }

    @Test
    void createProfile_shouldRejectNullRequest() {
        assertThrows(
                BadRequestException.class,
                () -> userProfileService.createProfile(null)
        );

        verifyNoInteractions(
                userProfileRepository,
                userProfileMapper,
                userPreferenceMapper,
                userPreferenceRepository
        );
    }

    @Test
    void createProfile_shouldRejectDuplicateProfile() {
        CreateUserProfileRequest request =
                CreateUserProfileRequest.builder().build();

        when(userProfileRepository.existsByUserId(userId))
                .thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> userProfileService.createProfile(request)
        );

        verify(userProfileRepository).existsByUserId(userId);
        verify(userProfileRepository, never()).save(any());
        verifyNoInteractions(userProfileMapper, userPreferenceMapper,
                userPreferenceRepository);
    }

    @Test
    void createProfile_shouldRequireUserContext() {
        UserContextHolder.clear();

        assertThrows(
                IllegalStateException.class,
                () -> userProfileService.createProfile(
                        CreateUserProfileRequest.builder().build()
                )
        );

        verifyNoInteractions(
                userProfileRepository,
                userProfileMapper,
                userPreferenceMapper,
                userPreferenceRepository
        );
    }

    @Test
    void getCurrentUserProfile_shouldReturnCurrentUsersProfile() {
        when(userProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(profile));
        when(userProfileMapper.toResponse(profile))
                .thenReturn(response);

        UserProfileResponse result =
                userProfileService.getCurrentUserProfile();

        assertSame(response, result);
        verify(userProfileRepository).findByUserId(userId);
        verify(userProfileMapper).toResponse(profile);
    }

    @Test
    void getCurrentUserProfile_shouldThrowWhenProfileIsMissing() {
        when(userProfileRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userProfileService.getCurrentUserProfile()
        );

        verify(userProfileMapper, never()).toResponse(any());
    }

    @Test
    void getProfileByUserId_shouldReturnRequestedProfile() {
        UUID requestedUserId = UUID.randomUUID();

        when(userProfileRepository.findByUserId(requestedUserId))
                .thenReturn(Optional.of(profile));
        when(userProfileMapper.toResponse(profile))
                .thenReturn(response);

        UserProfileResponse result =
                userProfileService.getProfileByUserId(requestedUserId);

        assertSame(response, result);
        verify(userProfileRepository).findByUserId(requestedUserId);
        verify(userProfileMapper).toResponse(profile);
    }

    @Test
    void getProfileByUserId_shouldThrowWhenProfileIsMissing() {
        UUID requestedUserId = UUID.randomUUID();

        when(userProfileRepository.findByUserId(requestedUserId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userProfileService.getProfileByUserId(requestedUserId)
        );

        verify(userProfileMapper, never()).toResponse(any());
    }

    @Test
    void updateProfile_shouldUpdateAndReturnProfile() {
        UpdateUserProfileRequest request =
                UpdateUserProfileRequest.builder()
                        .firstName("Updated")
                        .displayName("Updated User")
                        .build();

        when(userProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(profile));
        when(userProfileRepository.save(profile))
                .thenReturn(profile);
        when(userProfileMapper.toResponse(profile))
                .thenReturn(response);

        UserProfileResponse result =
                userProfileService.updateProfile(request);

        assertSame(response, result);
        verify(userProfileMapper).updateEntity(profile, request);
        verify(userProfileRepository).save(profile);
        verify(userProfileMapper).toResponse(profile);
    }

    @Test
    void updateProfile_shouldRejectNullRequest() {
        assertThrows(
                BadRequestException.class,
                () -> userProfileService.updateProfile(null)
        );

        verifyNoInteractions(
                userProfileRepository,
                userProfileMapper,
                userPreferenceMapper,
                userPreferenceRepository
        );
    }

    @Test
    void updateProfile_shouldThrowWhenProfileIsMissing() {
        UpdateUserProfileRequest request =
                UpdateUserProfileRequest.builder().build();

        when(userProfileRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userProfileService.updateProfile(request)
        );

        verify(userProfileRepository, never()).save(any());
    }

    @Test
    void updateProfile_shouldRequireUserContext() {
        UserContextHolder.clear();

        assertThrows(
                IllegalStateException.class,
                () -> userProfileService.updateProfile(
                        UpdateUserProfileRequest.builder().build()
                )
        );

        verifyNoInteractions(
                userProfileRepository,
                userProfileMapper,
                userPreferenceMapper,
                userPreferenceRepository
        );
    }

    @Test
    void deleteProfile_shouldDeleteExistingProfile() {
        when(userProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(profile));

        assertDoesNotThrow(() -> userProfileService.deleteProfile());

        verify(userProfileRepository).delete(profile);
    }

    @Test
    void deleteProfile_shouldThrowWhenProfileIsMissing() {
        when(userProfileRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userProfileService.deleteProfile()
        );

        verify(userProfileRepository, never()).delete(any());
    }

    @Test
    void deleteProfile_shouldRequireUserContext() {
        UserContextHolder.clear();

        assertThrows(
                IllegalStateException.class,
                () -> userProfileService.deleteProfile()
        );

        verifyNoInteractions(
                userProfileRepository,
                userProfileMapper,
                userPreferenceMapper,
                userPreferenceRepository
        );
    }
}