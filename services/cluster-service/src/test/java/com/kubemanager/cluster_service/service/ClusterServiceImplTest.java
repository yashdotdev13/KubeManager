package com.kubemanager.cluster_service.service;


import com.kubemanager.cluster_service.auth.UserContext;
import com.kubemanager.cluster_service.auth.UserContextHolder;
import com.kubemanager.cluster_service.dto.request.CreateClusterRequest;
import com.kubemanager.cluster_service.dto.request.UpdateClusterRequest;
import com.kubemanager.cluster_service.dto.response.ClusterResponse;
import com.kubemanager.cluster_service.dto.response.ClusterSummaryResponse;
import com.kubemanager.cluster_service.dto.response.NodeResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.enums.ClusterEnvironment;
import com.kubemanager.cluster_service.enums.ClusterProvider;
import com.kubemanager.cluster_service.enums.ClusterStatus;
import com.kubemanager.cluster_service.enums.PlatformType;
import com.kubemanager.cluster_service.kubernates.metadata.ClusterMetadata;
import com.kubemanager.cluster_service.kubernates.service.KubernetesConnectionService;
import com.kubemanager.cluster_service.mapper.ClusterMapper;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.impl.ClusterServiceImpl;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ConflictException;
import com.kubemanager.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClusterServiceImplTest {

    @Mock
    private ClusterRepository clusterRepository;

    @Mock
    private ClusterMapper clusterMapper;

    @Mock
    private KubernetesConnectionService kubernetesConnectionService;

    @InjectMocks
    private ClusterServiceImpl clusterService;

    private UUID userId;
    private UUID clusterId;
    private Cluster cluster;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        clusterId = UUID.randomUUID();

        UserContextHolder.set(UserContext.builder()
                .userId(userId)
                .username("test-user")
                .email("test@example.com")
                .roles(List.of("ROLE_USER"))
                .provider("LOCAL")
                .requestId("request-123")
                .correlationId("correlation-123")
                .build());

        cluster = Cluster.builder()
                .id(clusterId)
                .ownerId(userId)
                .name("test-cluster")
                .description("Test cluster")
                .provider(ClusterProvider.MINIKUBE)
                .environment(ClusterEnvironment.DEVELOPMENT)
                .status(ClusterStatus.CONNECTED)
                .platform(PlatformType.LINUX)
                .nodeCount(2)
                .namespaceCount(3)
                .encryptedKubeConfig("stored-config")
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void createCluster_shouldCreateClusterSuccessfully() {
        CreateClusterRequest request = CreateClusterRequest.builder()
                .name("test-cluster")
                .description("Test cluster")
                .provider(ClusterProvider.MINIKUBE)
                .environment(ClusterEnvironment.DEVELOPMENT)
                .build();

        ClusterResponse expected = ClusterResponse.builder()
                .id(clusterId)
                .name("test-cluster")
                .build();

        when(clusterRepository.existsByOwnerIdAndNameIgnoreCase(
                userId, "test-cluster")).thenReturn(false);
        when(clusterMapper.toEntity(request, userId)).thenReturn(cluster);
        when(clusterRepository.save(cluster)).thenReturn(cluster);
        when(clusterMapper.toResponse(cluster)).thenReturn(expected);

        ClusterResponse result = clusterService.createCluster(request);

        assertThat(result).isSameAs(expected);
        verify(clusterRepository).existsByOwnerIdAndNameIgnoreCase(
                userId, "test-cluster");
        verify(clusterRepository).save(cluster);
    }

    @Test
    void createCluster_shouldRejectNullRequest() {
        assertThatThrownBy(() -> clusterService.createCluster(null))
                .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(clusterRepository, clusterMapper);
    }

    @Test
    void createCluster_shouldRejectDuplicateName() {
        CreateClusterRequest request = CreateClusterRequest.builder()
                .name("test-cluster")
                .build();

        when(clusterRepository.existsByOwnerIdAndNameIgnoreCase(
                userId, "test-cluster")).thenReturn(true);

        assertThatThrownBy(() -> clusterService.createCluster(request))
                .isInstanceOf(ConflictException.class);

        verify(clusterRepository, never()).save(any());
        verify(clusterMapper, never()).toEntity(any(), any());
    }

    @Test
    void createCluster_shouldFailWhenUserContextIsMissing() {
        UserContextHolder.clear();

        assertThatThrownBy(() -> clusterService.createCluster(
                CreateClusterRequest.builder()
                        .name("test-cluster")
                        .build()))
                .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(clusterRepository, clusterMapper);
    }

    @Test
    void getMyClusters_shouldReturnCurrentUsersClusters() {
        List<Cluster> clusters = List.of(cluster);
        List<ClusterSummaryResponse> expected = List.of(
                ClusterSummaryResponse.builder()
                        .id(clusterId)
                        .name("test-cluster")
                        .build());

        when(clusterRepository.findAllByOwnerId(userId)).thenReturn(clusters);
        when(clusterMapper.toSummaryResponseList(clusters))
                .thenReturn(expected);

        List<ClusterSummaryResponse> result = clusterService.getMyClusters();

        assertThat(result).isEqualTo(expected);
        verify(clusterRepository).findAllByOwnerId(userId);
    }

    @Test
    void getMyClusters_shouldReturnEmptyListWhenNoClustersExist() {
        when(clusterRepository.findAllByOwnerId(userId))
                .thenReturn(List.of());
        when(clusterMapper.toSummaryResponseList(List.of()))
                .thenReturn(List.of());

        assertThat(clusterService.getMyClusters()).isEmpty();
    }

    @Test
    void getClusterById_shouldReturnOwnedCluster() {
        ClusterResponse expected = ClusterResponse.builder()
                .id(clusterId)
                .name("test-cluster")
                .build();

        when(clusterRepository.findByIdAndOwnerId(clusterId, userId))
                .thenReturn(Optional.of(cluster));
        when(clusterMapper.toResponse(cluster)).thenReturn(expected);

        ClusterResponse result = clusterService.getClusterById(clusterId);

        assertThat(result).isSameAs(expected);
        verify(clusterRepository).findByIdAndOwnerId(clusterId, userId);
    }

    @Test
    void getClusterById_shouldThrowWhenClusterIsNotOwnedOrMissing() {
        when(clusterRepository.findByIdAndOwnerId(clusterId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> clusterService.getClusterById(clusterId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(clusterMapper, never()).toResponse(any());
    }

    @Test
    void updateCluster_shouldUpdateOwnedCluster() {
        UpdateClusterRequest request = UpdateClusterRequest.builder()
                .name("updated-cluster")
                .description("Updated description")
                .environment(ClusterEnvironment.STAGING)
                .build();

        ClusterResponse expected = ClusterResponse.builder()
                .id(clusterId)
                .name("updated-cluster")
                .build();

        when(clusterRepository.findByIdAndOwnerId(clusterId, userId))
                .thenReturn(Optional.of(cluster));
        when(clusterRepository.save(cluster)).thenReturn(cluster);
        when(clusterMapper.toResponse(cluster)).thenReturn(expected);

        ClusterResponse result = clusterService.updateCluster(clusterId, request);

        assertThat(result).isSameAs(expected);
        verify(clusterMapper).updateEntity(cluster, request);
        verify(clusterRepository).save(cluster);
    }

    @Test
    void updateCluster_shouldRejectNullRequest() {
        assertThatThrownBy(() -> clusterService.updateCluster(clusterId, null))
                .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(clusterRepository, clusterMapper);
    }

    @Test
    void updateCluster_shouldThrowWhenClusterIsNotOwnedOrMissing() {
        UpdateClusterRequest request = UpdateClusterRequest.builder()
                .name("updated-cluster")
                .environment(ClusterEnvironment.STAGING)
                .build();

        when(clusterRepository.findByIdAndOwnerId(clusterId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> clusterService.updateCluster(clusterId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(clusterRepository, never()).save(any());
        verify(clusterMapper, never()).updateEntity(any(), any());
    }

    @Test
    void deleteCluster_shouldDeleteOwnedCluster() {
        when(clusterRepository.findByIdAndOwnerId(clusterId, userId))
                .thenReturn(Optional.of(cluster));

        clusterService.deleteCluster(clusterId);

        verify(clusterRepository).delete(cluster);
    }

    @Test
    void deleteCluster_shouldThrowWhenClusterIsNotOwnedOrMissing() {
        when(clusterRepository.findByIdAndOwnerId(clusterId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> clusterService.deleteCluster(clusterId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(clusterRepository, never()).delete(any());
    }

    @Test
    void connectCluster_shouldRejectNullOrEmptyFile() {
        assertThatThrownBy(() -> clusterService.connectCluster(clusterId, null))
                .isInstanceOf(BadRequestException.class);

        MultipartFile emptyFile = new MockMultipartFile(
                "kubeConfig", "config.yaml", "text/yaml", new byte[0]);

        assertThatThrownBy(() ->
                clusterService.connectCluster(clusterId, emptyFile))
                .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(clusterRepository, kubernetesConnectionService);
    }

    @Test
    void connectCluster_shouldThrowWhenClusterIsNotOwnedOrMissing() {
        MultipartFile file = new MockMultipartFile(
                "kubeConfig", "config.yaml", "text/yaml",
                "apiVersion: v1".getBytes());

        when(clusterRepository.findByIdAndOwnerId(clusterId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> clusterService.connectCluster(clusterId, file))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(kubernetesConnectionService);
    }

    @Test
    void connectCluster_shouldSaveConfigurationAndMetadata() {
        MultipartFile file = new MockMultipartFile(
                "kubeConfig", "config.yaml", "text/yaml",
                "apiVersion: v1".getBytes());

        ClusterMetadata metadata = ClusterMetadata.builder()
                .apiServer("https://k8s.example.test")
                .kubernetesVersion("v1.30.0")
                .platform(PlatformType.LINUX)
                .nodeCount(4)
                .namespaceCount(8)
                .status(ClusterStatus.CONNECTED)
                .lastHealthCheck(LocalDateTime.now())
                .build();

        ClusterResponse expected = ClusterResponse.builder()
                .id(clusterId)
                .name("test-cluster")
                .status(ClusterStatus.CONNECTED)
                .build();

        when(clusterRepository.findByIdAndOwnerId(clusterId, userId))
                .thenReturn(Optional.of(cluster));
        when(kubernetesConnectionService.connect(file)).thenReturn(metadata);
        when(clusterRepository.save(cluster)).thenReturn(cluster);
        when(clusterMapper.toResponse(cluster)).thenReturn(expected);

        ClusterResponse result = clusterService.connectCluster(clusterId, file);

        assertThat(result).isSameAs(expected);
        assertThat(cluster.getEncryptedKubeConfig())
                .isEqualTo("apiVersion: v1");
        assertThat(cluster.getApiServer())
                .isEqualTo("https://k8s.example.test");
        assertThat(cluster.getKubernetesVersion()).isEqualTo("v1.30.0");
        assertThat(cluster.getPlatform()).isEqualTo(PlatformType.LINUX);
        assertThat(cluster.getNodeCount()).isEqualTo(4);
        assertThat(cluster.getNamespaceCount()).isEqualTo(8);
        assertThat(cluster.getStatus()).isEqualTo(ClusterStatus.CONNECTED);

        verify(clusterRepository).save(cluster);
    }

    @Test
    void connectCluster_shouldTranslateFileReadFailure() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenThrow(new IOException("Read failed"));

        when(clusterRepository.findByIdAndOwnerId(clusterId, userId))
                .thenReturn(Optional.of(cluster));
        when(kubernetesConnectionService.connect(file))
                .thenReturn(ClusterMetadata.builder()
                        .status(ClusterStatus.CONNECTED)
                        .build());

        assertThatThrownBy(() -> clusterService.connectCluster(clusterId, file))
                .isInstanceOf(BadRequestException.class);

        verify(clusterRepository, never()).save(any());
    }

    @Test
    void getNodes_shouldReturnNodesForConnectedCluster() {
        List<NodeResponse> expected = List.of(
                NodeResponse.builder().name("node-1").status("READY").build());

        when(clusterRepository.findByIdAndOwnerId(clusterId, userId))
                .thenReturn(Optional.of(cluster));
        when(kubernetesConnectionService.getNodes("stored-config"))
                .thenReturn(expected);

        List<NodeResponse> result = clusterService.getNodes(clusterId);

        assertThat(result).isEqualTo(expected);
        verify(kubernetesConnectionService).getNodes("stored-config");
    }

    @Test
    void getNodes_shouldRejectClusterWithoutConfiguration() {
        cluster.setEncryptedKubeConfig(" ");

        when(clusterRepository.findByIdAndOwnerId(clusterId, userId))
                .thenReturn(Optional.of(cluster));

        assertThatThrownBy(() -> clusterService.getNodes(clusterId))
                .isInstanceOf(BadRequestException.class);

        verifyNoInteractions(kubernetesConnectionService);
    }

    @Test
    void getNodes_shouldThrowWhenClusterIsNotOwnedOrMissing() {
        when(clusterRepository.findByIdAndOwnerId(clusterId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> clusterService.getNodes(clusterId))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(kubernetesConnectionService);
    }
}