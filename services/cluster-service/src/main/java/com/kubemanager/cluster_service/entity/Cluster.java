package com.kubemanager.cluster_service.entity;


import com.kubemanager.cluster_service.enums.ClusterEnvironment;
import com.kubemanager.cluster_service.enums.ClusterProvider;
import com.kubemanager.cluster_service.enums.ClusterStatus;
import com.kubemanager.cluster_service.enums.PlatformType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "clusters",
        indexes = {

                @Index(
                        name = "idx_cluster_owner_id",
                        columnList = "ownerId"
                ),

                @Index(
                        name = "idx_cluster_name",
                        columnList = "name"
                ),

                @Index(
                        name = "idx_cluster_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cluster {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private ClusterProvider provider =
            ClusterProvider.OTHER;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private ClusterEnvironment environment =
            ClusterEnvironment.DEVELOPMENT;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private ClusterStatus status =
            ClusterStatus.CONNECTING;

    @Column(length = 300)
    private String apiServer;

    @Column(length = 50)
    private String kubernetesVersion;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private PlatformType platform =
            PlatformType.OTHER;

    @Builder.Default
    @Column(nullable = false)
    private Integer nodeCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer namespaceCount = 0;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String encryptedKubeConfig;

    private LocalDateTime lastHealthCheck;

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
