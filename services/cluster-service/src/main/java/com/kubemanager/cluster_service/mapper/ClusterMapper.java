package com.kubemanager.cluster_service.mapper;


import com.kubemanager.cluster_service.dto.request.CreateClusterRequest;
import com.kubemanager.cluster_service.dto.request.UpdateClusterRequest;
import com.kubemanager.cluster_service.dto.response.ClusterResponse;
import com.kubemanager.cluster_service.dto.response.ClusterSummaryResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ClusterMapper {

    public Cluster toEntity(
            CreateClusterRequest request,
            UUID ownerId
    ) {

        return Cluster.builder()

                .ownerId(ownerId)

                .name(request.getName())
                .description(request.getDescription())

                .provider(request.getProvider())
                .environment(request.getEnvironment())

                .build();
    }

    public void updateEntity(
            Cluster cluster,
            UpdateClusterRequest request
    ) {

        cluster.setName(request.getName());
        cluster.setDescription(request.getDescription());
        cluster.setEnvironment(request.getEnvironment());
    }

    public ClusterResponse toResponse(
            Cluster cluster
    ) {

        return ClusterResponse.builder()

                .id(cluster.getId())
                .ownerId(cluster.getOwnerId())

                .name(cluster.getName())
                .description(cluster.getDescription())

                .provider(cluster.getProvider())
                .environment(cluster.getEnvironment())
                .status(cluster.getStatus())

                .apiServer(cluster.getApiServer())
                .kubernetesVersion(cluster.getKubernetesVersion())
                .platform(cluster.getPlatform())

                .nodeCount(cluster.getNodeCount())
                .namespaceCount(cluster.getNamespaceCount())

                .lastHealthCheck(cluster.getLastHealthCheck())

                .createdAt(cluster.getCreatedAt())
                .updatedAt(cluster.getUpdatedAt())

                .build();
    }

    public ClusterSummaryResponse toSummaryResponse(
            Cluster cluster
    ) {

        return ClusterSummaryResponse.builder()

                .id(cluster.getId())
                .name(cluster.getName())
                .provider(cluster.getProvider())
                .status(cluster.getStatus())

                .build();
    }

    public List<ClusterSummaryResponse> toSummaryResponseList(
            List<Cluster> clusters
    ) {

        return clusters.stream()
                .map(this::toSummaryResponse)
                .toList();
    }

}