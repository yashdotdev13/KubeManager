package com.kubemanager.cluster_service.service.impl;

import com.kubemanager.cluster_service.dto.response.ClusterHealthResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.metadata.ClusterMetadata;
import com.kubemanager.cluster_service.kubernates.service.KubernetesConnectionService;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.ClusterHealthService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClusterHealthServiceImpl implements ClusterHealthService {

    private final ClusterRepository clusterRepository;
    private final KubernetesConnectionService kubernetesConnectionService;

    @Override
    public ClusterHealthResponse healthCheck(UUID clusterId) {

        log.info("Performing health check for cluster: {}", clusterId);

        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.CLUSTER_NOT_FOUND,
                        "Cluster not found with id: " + clusterId
                ));

        if (cluster.getEncryptedKubeConfig() == null ||
                cluster.getEncryptedKubeConfig().isBlank()) {

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Cluster kubeconfig is not available."
            );
        }

        ClusterMetadata metadata = kubernetesConnectionService.connect(
                cluster.getEncryptedKubeConfig()
        );

        cluster.setStatus(metadata.getStatus());
        cluster.setApiServer(metadata.getApiServer());
        cluster.setKubernetesVersion(metadata.getKubernetesVersion());
        cluster.setPlatform(metadata.getPlatform());
        cluster.setNodeCount(metadata.getNodeCount());
        cluster.setNamespaceCount(metadata.getNamespaceCount());
        cluster.setLastHealthCheck(metadata.getLastHealthCheck());

        Cluster updatedCluster = clusterRepository.save(cluster);

        log.info(
                "Health check completed successfully for cluster: {}",
                clusterId
        );

        return ClusterHealthResponse.builder()
                .clusterId(updatedCluster.getId())
                .clusterName(updatedCluster.getName())
                .status(updatedCluster.getStatus())
                .apiServer(updatedCluster.getApiServer())
                .kubernetesVersion(updatedCluster.getKubernetesVersion())
                .nodeCount(updatedCluster.getNodeCount())
                .namespaceCount(updatedCluster.getNamespaceCount())
                .lastHealthCheck(updatedCluster.getLastHealthCheck())
                .updatedAt(updatedCluster.getUpdatedAt())
                .build();
    }
}