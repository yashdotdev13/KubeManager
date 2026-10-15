package com.kubemanager.cluster_service.mapper;

import com.kubemanager.cluster_service.dto.request.CreateClusterRequest;
import com.kubemanager.cluster_service.dto.request.UpdateClusterRequest;
import com.kubemanager.cluster_service.dto.response.ClusterResponse;
import com.kubemanager.cluster_service.dto.response.ClusterSummaryResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.enums.ClusterEnvironment;
import com.kubemanager.cluster_service.enums.ClusterProvider;
import com.kubemanager.cluster_service.enums.ClusterStatus;
import com.kubemanager.cluster_service.enums.PlatformType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ClusterMapperTest {

    private ClusterMapper clusterMapper;

    @BeforeEach
    void setUp() {
        clusterMapper = new ClusterMapper();
    }

    @Test
    void toEntity_shouldMapCreateRequestAndOwnerId() {
        UUID ownerId = UUID.randomUUID();

        CreateClusterRequest request = CreateClusterRequest.builder()
                .name("local-cluster")
                .description("Local development cluster")
                .provider(ClusterProvider.MINIKUBE)
                .environment(ClusterEnvironment.DEVELOPMENT)
                .build();

        Cluster result = clusterMapper.toEntity(request, ownerId);

        assertThat(result.getOwnerId()).isEqualTo(ownerId);
        assertThat(result.getName()).isEqualTo("local-cluster");
        assertThat(result.getDescription())
                .isEqualTo("Local development cluster");
        assertThat(result.getProvider()).isEqualTo(ClusterProvider.MINIKUBE);
        assertThat(result.getEnvironment())
                .isEqualTo(ClusterEnvironment.DEVELOPMENT);

        assertThat(result.getStatus()).isEqualTo(ClusterStatus.CONNECTING);
        assertThat(result.getPlatform()).isEqualTo(PlatformType.OTHER);
        assertThat(result.getNodeCount()).isZero();
        assertThat(result.getNamespaceCount()).isZero();
    }

    @Test
    void updateEntity_shouldUpdateOnlyMappedFields() {
        Cluster cluster = Cluster.builder()
                .id(UUID.randomUUID())
                .ownerId(UUID.randomUUID())
                .name("old-name")
                .description("Old description")
                .provider(ClusterProvider.EKS)
                .environment(ClusterEnvironment.DEVELOPMENT)
                .status(ClusterStatus.CONNECTED)
                .apiServer("https://example.test")
                .kubernetesVersion("v1.30.0")
                .platform(PlatformType.LINUX)
                .nodeCount(3)
                .namespaceCount(5)
                .build();

        UpdateClusterRequest request = UpdateClusterRequest.builder()
                .name("new-name")
                .description("Updated description")
                .environment(ClusterEnvironment.STAGING)
                .build();

        clusterMapper.updateEntity(cluster, request);

        assertThat(cluster.getName()).isEqualTo("new-name");
        assertThat(cluster.getDescription())
                .isEqualTo("Updated description");
        assertThat(cluster.getEnvironment())
                .isEqualTo(ClusterEnvironment.STAGING);

        assertThat(cluster.getId()).isNotNull();
        assertThat(cluster.getProvider()).isEqualTo(ClusterProvider.EKS);
        assertThat(cluster.getStatus()).isEqualTo(ClusterStatus.CONNECTED);
        assertThat(cluster.getApiServer()).isEqualTo("https://example.test");
        assertThat(cluster.getKubernetesVersion()).isEqualTo("v1.30.0");
        assertThat(cluster.getPlatform()).isEqualTo(PlatformType.LINUX);
        assertThat(cluster.getNodeCount()).isEqualTo(3);
        assertThat(cluster.getNamespaceCount()).isEqualTo(5);
    }

    @Test
    void toResponse_shouldMapClusterFields() {
        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        Cluster cluster = Cluster.builder()
                .id(id)
                .ownerId(ownerId)
                .name("production-cluster")
                .description("Production Kubernetes cluster")
                .provider(ClusterProvider.EKS)
                .environment(ClusterEnvironment.PRODUCTION)
                .status(ClusterStatus.CONNECTED)
                .apiServer("https://k8s.example.test")
                .kubernetesVersion("v1.30.0")
                .platform(PlatformType.LINUX)
                .nodeCount(4)
                .namespaceCount(12)
                .lastHealthCheck(updatedAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        ClusterResponse result = clusterMapper.toResponse(cluster);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getOwnerId()).isEqualTo(ownerId);
        assertThat(result.getName()).isEqualTo("production-cluster");
        assertThat(result.getDescription())
                .isEqualTo("Production Kubernetes cluster");
        assertThat(result.getProvider()).isEqualTo(ClusterProvider.EKS);
        assertThat(result.getEnvironment())
                .isEqualTo(ClusterEnvironment.PRODUCTION);
        assertThat(result.getStatus()).isEqualTo(ClusterStatus.CONNECTED);
        assertThat(result.getApiServer()).isEqualTo("https://k8s.example.test");
        assertThat(result.getKubernetesVersion()).isEqualTo("v1.30.0");
        assertThat(result.getPlatform()).isEqualTo(PlatformType.LINUX);
        assertThat(result.getNodeCount()).isEqualTo(4);
        assertThat(result.getNamespaceCount()).isEqualTo(12);
        assertThat(result.getLastHealthCheck()).isEqualTo(updatedAt);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void toSummaryResponseList_shouldMapAllClusters() {
        Cluster first = Cluster.builder()
                .id(UUID.randomUUID())
                .name("cluster-one")
                .provider(ClusterProvider.MINIKUBE)
                .status(ClusterStatus.CONNECTED)
                .build();

        Cluster second = Cluster.builder()
                .id(UUID.randomUUID())
                .name("cluster-two")
                .provider(ClusterProvider.EKS)
                .status(ClusterStatus.DISCONNECTED)
                .build();

        List<ClusterSummaryResponse> result =
                clusterMapper.toSummaryResponseList(List.of(first, second));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(first.getId());
        assertThat(result.get(0).getName()).isEqualTo("cluster-one");
        assertThat(result.get(0).getProvider())
                .isEqualTo(ClusterProvider.MINIKUBE);
        assertThat(result.get(0).getStatus())
                .isEqualTo(ClusterStatus.CONNECTED);

        assertThat(result.get(1).getId()).isEqualTo(second.getId());
        assertThat(result.get(1).getName()).isEqualTo("cluster-two");
        assertThat(result.get(1).getProvider()).isEqualTo(ClusterProvider.EKS);
        assertThat(result.get(1).getStatus())
                .isEqualTo(ClusterStatus.DISCONNECTED);
    }

    @Test
    void toSummaryResponseList_shouldReturnEmptyListWhenInputIsEmpty() {
        List<ClusterSummaryResponse> result =
                clusterMapper.toSummaryResponseList(List.of());

        assertThat(result).isEmpty();
    }
}