package com.kubemanager.user_service.dtos.response;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceResponse {

    private Boolean emailNotification;

    private Boolean slackNotification;

    private Boolean desktopNotification;

}