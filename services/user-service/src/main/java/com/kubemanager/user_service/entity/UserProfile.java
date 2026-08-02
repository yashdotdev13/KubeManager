package com.kubemanager.user_service.entity;



import com.kubemanager.user_service.entity.embeddable.ContactInfo;
import com.kubemanager.user_service.entity.embeddable.PersonalInfo;
import com.kubemanager.user_service.entity.embeddable.ProfessionalInfo;
import com.kubemanager.user_service.enums.Language;
import com.kubemanager.user_service.enums.ProfileVisibility;
import com.kubemanager.user_service.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "user_profiles",
        indexes = {
                @Index(name = "idx_user_profile_user_id", columnList = "userId")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Embedded
    private PersonalInfo personalInfo;

    @Embedded
    private ProfessionalInfo professionalInfo;

    @Embedded
    private ContactInfo contactInfo;

    @Column(length = 500)
    private String bio;

    @Column(length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private ProfileVisibility visibility =
            ProfileVisibility.ORGANIZATION;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private UserStatus status =
            UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private Language language =
            Language.ENGLISH;

    @Column(length = 100)
    @Builder.Default
    private String timezone = "Asia/Kolkata";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {

        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {

        updatedAt = LocalDateTime.now();
    }

}