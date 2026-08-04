package com.kubemanager.user_service.dtos.response;

import com.kubemanager.user_service.enums.Language;
import com.kubemanager.user_service.enums.ProfileVisibility;
import com.kubemanager.user_service.enums.UserStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private UUID userId;

    private PersonalInfoResponse personalInfo;

    private ProfessionalInfoResponse professionalInfo;

    private ContactInfoResponse contactInfo;

    private String bio;

    private String avatarUrl;

    private ProfileVisibility visibility;

    private UserStatus status;

    private Language language;

    private String timezone;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}