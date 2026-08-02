package com.kubemanager.user_service.dtos.response;

import com.kubemanager.user_service.enums.Theme;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspacePreferenceResponse {

    private Theme theme;

    private String defaultCluster;

    private String defaultNamespace;

}
