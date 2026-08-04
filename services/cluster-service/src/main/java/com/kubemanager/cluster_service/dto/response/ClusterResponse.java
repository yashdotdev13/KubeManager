package com.kubemanager.cluster_service.dto.response;


import com.kubemanager.cluster_service.enums.ClusterEnvironment;
import com.kubemanager.cluster_service.enums.ClusterProvider;
import com.kubemanager.cluster_service.enums.ClusterStatus;
import com.kubemanager.cluster_service.enums.PlatformType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusterResponse {

    private UUID id;

    private UUID ownerId;

    private String name;

    private String description;

    private ClusterProvider provider;

    private ClusterEnvironment environment;

    private ClusterStatus status;

    private String apiServer;

    private String kubernetesVersion;

    private PlatformType platform;

    private Integer nodeCount;

    private Integer namespaceCount;

    private LocalDateTime lastHealthCheck;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}