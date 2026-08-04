package com.kubemanager.user_service.entity.embeddable;

import com.kubemanager.user_service.enums.Theme;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class WorkspacePreference {

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private Theme theme = Theme.SYSTEM;

    @Column(length = 100)
    private String defaultCluster;

    @Column(length = 100)
    private String defaultNamespace;

}