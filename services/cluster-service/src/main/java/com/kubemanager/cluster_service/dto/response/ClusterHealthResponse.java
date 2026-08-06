package com.kubemanager.cluster_service.dto.response;


import com.kubemanager.cluster_service.enums.ClusterStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClusterHealthResponse {

    private UUID clusterId;
    private String clusterName;

    private ClusterStatus status;

    private String apiServer;

    private String kubernetesVersion;

    private Integer nodeCount;

    private Integer namespaceCount;

    private LocalDateTime lastHealthCheck;

    private LocalDateTime updatedAt;
}
