package com.kubemanager.cluster_service.kubernates.metadata;


import com.kubemanager.cluster_service.enums.ClusterStatus;
import com.kubemanager.cluster_service.enums.PlatformType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClusterMetadata {


    private String apiServer;

    private String kubernetesVersion;

    private PlatformType platform;

    private Integer nodeCount;

    private Integer namespaceCount;

    private ClusterStatus status;

    private LocalDateTime lastHealthCheck;
}
