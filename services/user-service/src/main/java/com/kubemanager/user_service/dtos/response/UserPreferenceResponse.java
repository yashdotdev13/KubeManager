package com.kubemanager.user_service.dtos.response;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceResponse {

    private NotificationPreferenceResponse notificationPreference;

    private WorkspacePreferenceResponse workspacePreference;

    private AIPreferenceResponse aiPreference;

}
