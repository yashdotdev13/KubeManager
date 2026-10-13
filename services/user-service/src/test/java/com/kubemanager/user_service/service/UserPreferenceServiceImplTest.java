package com.kubemanager.user_service.service;

import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ResourceNotFoundException;
import com.kubemanager.user_service.auth.UserContext;
import com.kubemanager.user_service.auth.UserContextHolder;
import com.kubemanager.user_service.dtos.request.UpdateUserPreferenceRequest;
import com.kubemanager.user_service.dtos.response.UserPreferenceResponse;
import com.kubemanager.user_service.entity.UserPreference;
import com.kubemanager.user_service.enums.Theme;
import com.kubemanager.user_service.mapper.UserPreferenceMapper;
import com.kubemanager.user_service.repository.UserPreferenceRepository;
import com.kubemanager.user_service.service.Impl.UserPreferenceServiceImpl;
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
class UserPreferenceServiceImplTest {

    @Mock
    private UserPreferenceRepository userPreferenceRepository;

    @Mock
    private UserPreferenceMapper userPreferenceMapper;

    @InjectMocks
    private UserPreferenceServiceImpl userPreferenceService;

    private UUID userId;
    private UserPreference preference;
    private UserPreferenceResponse response;

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

        preference = UserPreference.builder()
                .userId(userId)
                .build();

        response = UserPreferenceResponse.builder().build();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void getUserPreference_shouldReturnMappedPreferences() {
        when(userPreferenceRepository.findByUserId(userId))
                .thenReturn(Optional.of(preference));
        when(userPreferenceMapper.toResponse(preference))
                .thenReturn(response);

        UserPreferenceResponse result =
                userPreferenceService.getUserPreference();

        assertSame(response, result);
        verify(userPreferenceRepository).findByUserId(userId);
        verify(userPreferenceMapper).toResponse(preference);
    }

    @Test
    void getUserPreference_shouldThrowWhenPreferencesDoNotExist() {
        when(userPreferenceRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userPreferenceService.getUserPreference()
        );

        verify(userPreferenceMapper, never()).toResponse(any());
    }

    @Test
    void getUserPreference_shouldThrowWhenUserContextIsMissing() {
        UserContextHolder.clear();

        assertThrows(
                IllegalStateException.class,
                () -> userPreferenceService.getUserPreference()
        );

        verifyNoInteractions(userPreferenceRepository, userPreferenceMapper);
    }

    @Test
    void updatePreferences_shouldRejectNullRequest() {
        assertThrows(
                BadRequestException.class,
                () -> userPreferenceService.updatePreferences(null)
        );

        verifyNoInteractions(userPreferenceRepository, userPreferenceMapper);
    }

    @Test
    void updatePreferences_shouldUpdateAndReturnSavedPreferences() {
        UpdateUserPreferenceRequest request =
                UpdateUserPreferenceRequest.builder()
                        .theme(Theme.DARK)
                        .defaultCluster("production")
                        .defaultNamespace("monitoring")
                        .emailNotification(false)
                        .slackNotification(true)
                        .desktopNotification(false)
                        .aiSuggestionsEnabled(false)
                        .allowAiClusterActions(true)
                        .build();

        when(userPreferenceRepository.findByUserId(userId))
                .thenReturn(Optional.of(preference));
        when(userPreferenceRepository.save(preference))
                .thenReturn(preference);
        when(userPreferenceMapper.toResponse(preference))
                .thenReturn(response);

        UserPreferenceResponse result =
                userPreferenceService.updatePreferences(request);

        assertSame(response, result);
        verify(userPreferenceMapper).updateEntity(preference, request);
        verify(userPreferenceRepository).save(preference);
        verify(userPreferenceMapper).toResponse(preference);
    }

    @Test
    void updatePreferences_shouldThrowWhenPreferencesDoNotExist() {
        UpdateUserPreferenceRequest request =
                UpdateUserPreferenceRequest.builder().build();

        when(userPreferenceRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userPreferenceService.updatePreferences(request)
        );

        verify(userPreferenceRepository, never()).save(any());
    }

    @Test
    void resetPreferences_shouldResetAndSaveExistingPreferences() {
        when(userPreferenceRepository.findByUserId(userId))
                .thenReturn(Optional.of(preference));
        when(userPreferenceRepository.save(preference))
                .thenReturn(preference);
        when(userPreferenceMapper.toResponse(preference))
                .thenReturn(response);

        UserPreferenceResponse result =
                userPreferenceService.resetPreferences();

        assertSame(response, result);
        verify(userPreferenceMapper).resetToDefault(preference);
        verify(userPreferenceRepository).save(preference);
        verify(userPreferenceMapper).toResponse(preference);
    }

    @Test
    void resetPreferences_shouldThrowWhenPreferencesDoNotExist() {
        when(userPreferenceRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userPreferenceService.resetPreferences()
        );

        verify(userPreferenceRepository, never()).save(any());
    }

    @Test
    void resetPreferences_shouldThrowWhenUserContextIsMissing() {
        UserContextHolder.clear();

        assertThrows(
                IllegalStateException.class,
                () -> userPreferenceService.resetPreferences()
        );

        verifyNoInteractions(userPreferenceRepository, userPreferenceMapper);
    }
}

