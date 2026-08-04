package com.kubemanager.user_service.mapper;

import com.kubemanager.user_service.dtos.request.UpdateUserPreferenceRequest;
import com.kubemanager.user_service.dtos.response.AIPreferenceResponse;
import com.kubemanager.user_service.dtos.response.NotificationPreferenceResponse;
import com.kubemanager.user_service.dtos.response.UserPreferenceResponse;
import com.kubemanager.user_service.dtos.response.WorkspacePreferenceResponse;
import com.kubemanager.user_service.entity.UserPreference;
import com.kubemanager.user_service.entity.embeddable.AIPreference;
import com.kubemanager.user_service.entity.embeddable.NotificationPreference;
import com.kubemanager.user_service.entity.embeddable.WorkspacePreference;
import com.kubemanager.user_service.enums.Theme;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserPreferenceMapper {

    public void updateEntity(
            UserPreference preference,
            UpdateUserPreferenceRequest request
    ) {

        preference.getWorkspacePreference()
                .setTheme(request.getTheme());

        preference.getWorkspacePreference()
                .setDefaultCluster(request.getDefaultCluster());

        preference.getWorkspacePreference()
                .setDefaultNamespace(request.getDefaultNamespace());

        preference.getNotificationPreference()
                .setEmailNotification(request.getEmailNotification());

        preference.getNotificationPreference()
                .setSlackNotification(request.getSlackNotification());

        preference.getNotificationPreference()
                .setDesktopNotification(request.getDesktopNotification());

        preference.getAiPreference()
                .setAiSuggestionsEnabled(request.getAiSuggestionsEnabled());

        preference.getAiPreference()
                .setAllowAiClusterActions(request.getAllowAiClusterActions());
    }

    public UserPreferenceResponse toResponse(
            UserPreference preference
    ) {

        return UserPreferenceResponse.builder()

                .workspacePreference(
                        WorkspacePreferenceResponse.builder()
                                .theme(preference.getWorkspacePreference().getTheme())
                                .defaultCluster(preference.getWorkspacePreference().getDefaultCluster())
                                .defaultNamespace(preference.getWorkspacePreference().getDefaultNamespace())
                                .build()
                )

                .notificationPreference(
                        NotificationPreferenceResponse.builder()
                                .emailNotification(preference.getNotificationPreference().getEmailNotification())
                                .slackNotification(preference.getNotificationPreference().getSlackNotification())
                                .desktopNotification(preference.getNotificationPreference().getDesktopNotification())
                                .build()
                )

                .aiPreference(
                        AIPreferenceResponse.builder()
                                .aiSuggestionsEnabled(preference.getAiPreference().getAiSuggestionsEnabled())
                                .allowAiClusterActions(preference.getAiPreference().getAllowAiClusterActions())
                                .build()
                )

                .build();
    }



    public void resetToDefault(UserPreference preference) {

        preference.getWorkspacePreference().setTheme(Theme.SYSTEM);
        preference.getWorkspacePreference().setDefaultCluster(null);
        preference.getWorkspacePreference().setDefaultNamespace(null);

        preference.getNotificationPreference().setEmailNotification(true);
        preference.getNotificationPreference().setSlackNotification(false);
        preference.getNotificationPreference().setDesktopNotification(true);

        preference.getAiPreference().setAiSuggestionsEnabled(true);
        preference.getAiPreference().setAllowAiClusterActions(false);
    }


    public UserPreference toEntity(UUID userId) {

        return UserPreference.builder()

                .userId(userId)

                .workspacePreference(
                        WorkspacePreference.builder()
                                .theme(Theme.SYSTEM)
                                .defaultCluster(null)
                                .defaultNamespace(null)
                                .build()
                )

                .notificationPreference(
                        NotificationPreference.builder()
                                .emailNotification(true)
                                .slackNotification(false)
                                .desktopNotification(true)
                                .build()
                )

                .aiPreference(
                        AIPreference.builder()
                                .aiSuggestionsEnabled(true)
                                .allowAiClusterActions(false)
                                .build()
                )

                .build();
    }

}