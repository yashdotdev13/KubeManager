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
public class NotificationPreference {

    @Builder.Default
    @Column(nullable = false)
    private Boolean emailNotification = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean slackNotification = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean desktopNotification = true;

}