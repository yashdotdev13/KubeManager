package com.kubemanager.user_service.dtos.request;


import com.kubemanager.user_service.enums.Language;
import com.kubemanager.user_service.enums.Theme;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserPreferenceRequest {

    private Theme theme;
    private Language language;

    private String defaultCluster;

    private String defaultNamespace;

    private boolean emailNotification;

    private boolean slackNotification;

    private boolean desktopNotification;

    private Boolean aiSuggestionsEnabled;

    private Boolean allowAiClusterActions;
}
