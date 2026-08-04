package com.kubemanager.user_service.entity;

import com.kubemanager.user_service.entity.embeddable.AIPreference;
import com.kubemanager.user_service.entity.embeddable.NotificationPreference;
import com.kubemanager.user_service.entity.embeddable.WorkspacePreference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "user_preferences",
        indexes = {
                @Index(name = "idx_user_preference_user_id", columnList = "userId")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(nullable = false, unique = true)
    private UUID userId;

    @Embedded
    private NotificationPreference notificationPreference;

    @Embedded
    private WorkspacePreference workspacePreference;

    @Embedded
    private AIPreference aiPreference;

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