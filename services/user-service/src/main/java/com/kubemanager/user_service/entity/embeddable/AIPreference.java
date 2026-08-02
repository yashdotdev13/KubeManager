package com.kubemanager.user_service.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class AIPreference {

    @Builder.Default
    @Column(nullable = false)
    private Boolean aiSuggestionsEnabled = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean allowAiClusterActions = false;

}
