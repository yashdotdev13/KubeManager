package com.kubemanager.user_service.mapper;

import com.kubemanager.user_service.dtos.request.UpdateUserPreferenceRequest;
import com.kubemanager.user_service.dtos.response.UserPreferenceResponse;
import com.kubemanager.user_service.entity.UserPreference;
import com.kubemanager.user_service.enums.Theme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserPreferenceMapperTest {

    private UserPreferenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserPreferenceMapper();
    }

    @Test
    void toEntity_shouldCreatePreferencesWithDefaultValues() {
        UUID userId = UUID.randomUUID();

        UserPreference preference = mapper.toEntity(userId);

        assertNotNull(preference);
        assertEquals(userId, preference.getUserId());

        assertNotNull(preference.getWorkspacePreference());
        assertEquals(
                Theme.SYSTEM,
                preference.getWorkspacePreference().getTheme()
        );
        assertNull(
                preference.getWorkspacePreference().getDefaultCluster()
        );
        assertNull(
                preference.getWorkspacePreference().getDefaultNamespace()
        );

        assertNotNull(preference.getNotificationPreference());
        assertEquals(
                true,
                preference.getNotificationPreference().getEmailNotification()
        );
        assertEquals(
                false,
                preference.getNotificationPreference().getSlackNotification()
        );
        assertEquals(
                true,
                preference.getNotificationPreference().getDesktopNotification()
        );

        assertNotNull(preference.getAiPreference());
        assertEquals(
                true,
                preference.getAiPreference().getAiSuggestionsEnabled()
        );
        assertEquals(
                false,
                preference.getAiPreference().getAllowAiClusterActions()
        );
    }

    @Test
    void toResponse_shouldMapAllPreferenceFields() {
        UUID userId = UUID.randomUUID();
        UserPreference preference = mapper.toEntity(userId);

        preference.getWorkspacePreference().setTheme(Theme.DARK);
        preference.getWorkspacePreference().setDefaultCluster("production");
        preference.getWorkspacePreference().setDefaultNamespace("monitoring");

        preference.getNotificationPreference().setEmailNotification(false);
        preference.getNotificationPreference().setSlackNotification(true);
        preference.getNotificationPreference().setDesktopNotification(false);

        preference.getAiPreference().setAiSuggestionsEnabled(false);
        preference.getAiPreference().setAllowAiClusterActions(true);

        UserPreferenceResponse response = mapper.toResponse(preference);

        assertNotNull(response);

        assertEquals(
                Theme.DARK,
                response.getWorkspacePreference().getTheme()
        );
        assertEquals(
                "production",
                response.getWorkspacePreference().getDefaultCluster()
        );
        assertEquals(
                "monitoring",
                response.getWorkspacePreference().getDefaultNamespace()
        );

        assertEquals(
                false,
                response.getNotificationPreference().getEmailNotification()
        );
        assertEquals(
                true,
                response.getNotificationPreference().getSlackNotification()
        );
        assertEquals(
                false,
                response.getNotificationPreference().getDesktopNotification()
        );

        assertEquals(
                false,
                response.getAiPreference().getAiSuggestionsEnabled()
        );
        assertEquals(
                true,
                response.getAiPreference().getAllowAiClusterActions()
        );
    }

    @Test
    void updateEntity_shouldApplyRequestValues() {
        UserPreference preference = mapper.toEntity(UUID.randomUUID());

        UpdateUserPreferenceRequest request =
                UpdateUserPreferenceRequest.builder()
                        .theme(Theme.DARK)
                        .defaultCluster("staging")
                        .defaultNamespace("kube-system")
                        .emailNotification(false)
                        .slackNotification(true)
                        .desktopNotification(false)
                        .aiSuggestionsEnabled(false)
                        .allowAiClusterActions(true)
                        .build();

        mapper.updateEntity(preference, request);

        assertEquals(
                Theme.DARK,
                preference.getWorkspacePreference().getTheme()
        );
        assertEquals(
                "staging",
                preference.getWorkspacePreference().getDefaultCluster()
        );
        assertEquals(
                "kube-system",
                preference.getWorkspacePreference().getDefaultNamespace()
        );

        assertEquals(
                false,
                preference.getNotificationPreference().getEmailNotification()
        );
        assertEquals(
                true,
                preference.getNotificationPreference().getSlackNotification()
        );
        assertEquals(
                false,
                preference.getNotificationPreference().getDesktopNotification()
        );

        assertEquals(
                false,
                preference.getAiPreference().getAiSuggestionsEnabled()
        );
        assertEquals(
                true,
                preference.getAiPreference().getAllowAiClusterActions()
        );
    }

    @Test
    void resetToDefault_shouldRestoreAllDefaultValues() {
        UserPreference preference = mapper.toEntity(UUID.randomUUID());

        preference.getWorkspacePreference().setTheme(Theme.DARK);
        preference.getWorkspacePreference().setDefaultCluster("production");
        preference.getWorkspacePreference().setDefaultNamespace("monitoring");

        preference.getNotificationPreference().setEmailNotification(false);
        preference.getNotificationPreference().setSlackNotification(true);
        preference.getNotificationPreference().setDesktopNotification(false);

        preference.getAiPreference().setAiSuggestionsEnabled(false);
        preference.getAiPreference().setAllowAiClusterActions(true);

        mapper.resetToDefault(preference);

        assertEquals(
                Theme.SYSTEM,
                preference.getWorkspacePreference().getTheme()
        );
        assertNull(
                preference.getWorkspacePreference().getDefaultCluster()
        );
        assertNull(
                preference.getWorkspacePreference().getDefaultNamespace()
        );

        assertEquals(
                true,
                preference.getNotificationPreference().getEmailNotification()
        );
        assertEquals(
                false,
                preference.getNotificationPreference().getSlackNotification()
        );
        assertEquals(
                true,
                preference.getNotificationPreference().getDesktopNotification()
        );

        assertEquals(
                true,
                preference.getAiPreference().getAiSuggestionsEnabled()
        );
        assertEquals(
                false,
                preference.getAiPreference().getAllowAiClusterActions()
        );
    }
}
