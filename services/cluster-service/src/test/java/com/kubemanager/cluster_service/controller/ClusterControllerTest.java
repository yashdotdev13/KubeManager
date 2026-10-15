package com.kubemanager.cluster_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kubemanager.cluster_service.dto.request.CreateClusterRequest;
import com.kubemanager.cluster_service.dto.request.UpdateClusterRequest;
import com.kubemanager.cluster_service.dto.response.ClusterResponse;
import com.kubemanager.cluster_service.dto.response.ClusterSummaryResponse;
import com.kubemanager.cluster_service.enums.ClusterEnvironment;
import com.kubemanager.cluster_service.enums.ClusterProvider;
import com.kubemanager.cluster_service.enums.ClusterStatus;
import com.kubemanager.cluster_service.service.ClusterService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClusterController.class)
class ClusterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClusterService clusterService;

    @Test
    void createCluster_shouldReturnCreated() throws Exception {
        CreateClusterRequest request = new CreateClusterRequest();
        request.setName("local-cluster");
        request.setDescription("Local Kubernetes cluster");
        request.setProvider(ClusterProvider.MINIKUBE);
        request.setEnvironment(ClusterEnvironment.DEVELOPMENT);

        ClusterResponse response = clusterResponse();

        when(clusterService.createCluster(any(CreateClusterRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/clusters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("local-cluster"));

        verify(clusterService).createCluster(any(CreateClusterRequest.class));
    }

    @Test
    void createCluster_shouldRejectInvalidRequest() throws Exception {
        CreateClusterRequest request = new CreateClusterRequest();
        request.setName("");
        request.setDescription("Invalid request");
        request.setProvider(ClusterProvider.MINIKUBE);
        request.setEnvironment(ClusterEnvironment.DEVELOPMENT);

        mockMvc.perform(post("/api/v1/clusters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(clusterService);
    }

    @Test
    void getMyClusters_shouldReturnClusterList() throws Exception {
        when(clusterService.getMyClusters())
                .thenReturn(List.of(summaryResponse()));

        mockMvc.perform(get("/api/v1/clusters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("local-cluster"));

        verify(clusterService).getMyClusters();
    }

    @Test
    void getClusterById_shouldReturnCluster() throws Exception {
        UUID clusterId = UUID.randomUUID();

        when(clusterService.getClusterById(clusterId))
                .thenReturn(clusterResponse());

        mockMvc.perform(get("/api/v1/clusters/{clusterId}", clusterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("local-cluster"));

        verify(clusterService).getClusterById(clusterId);
    }

    @Test
    void updateCluster_shouldReturnUpdatedCluster() throws Exception {
        UUID clusterId = UUID.randomUUID();

        UpdateClusterRequest request = new UpdateClusterRequest();
        request.setName("updated-cluster");
        request.setDescription("Updated description");
        request.setEnvironment(ClusterEnvironment.STAGING);

        ClusterResponse response = clusterResponse();
        response.setName("updated-cluster");

        when(clusterService.updateCluster(
                eq(clusterId), any(UpdateClusterRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/clusters/{clusterId}", clusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("updated-cluster"));

        verify(clusterService).updateCluster(
                eq(clusterId), any(UpdateClusterRequest.class));
    }

    @Test
    void deleteCluster_shouldReturnSuccess() throws Exception {
        UUID clusterId = UUID.randomUUID();

        doNothing().when(clusterService).deleteCluster(clusterId);

        mockMvc.perform(delete("/api/v1/clusters/{clusterId}", clusterId))
                .andExpect(status().isOk());

        verify(clusterService).deleteCluster(clusterId);
    }

    @Test
    void connectCluster_shouldAcceptKubeConfigUpload() throws Exception {
        UUID clusterId = UUID.randomUUID();

        MockMultipartFile kubeConfig = new MockMultipartFile(
                "kubeConfig",
                "config.yaml",
                "application/yaml",
                "apiVersion: v1".getBytes()
        );

        when(clusterService.connectCluster(eq(clusterId), any()))
                .thenReturn(clusterResponse());

        mockMvc.perform(multipart(
                        "/api/v1/clusters/{clusterId}/connect", clusterId)
                        .file(kubeConfig))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("local-cluster"));

        verify(clusterService).connectCluster(eq(clusterId), any());
    }

    private ClusterResponse clusterResponse() {
        ClusterResponse response = new ClusterResponse();
        response.setId(UUID.randomUUID());
        response.setName("local-cluster");
        response.setProvider(ClusterProvider.MINIKUBE);
        response.setEnvironment(ClusterEnvironment.DEVELOPMENT);
        response.setStatus(ClusterStatus.CONNECTED);
        return response;
    }

    private ClusterSummaryResponse summaryResponse() {
        ClusterSummaryResponse response = new ClusterSummaryResponse();
        response.setId(UUID.randomUUID());
        response.setName("local-cluster");
        response.setProvider(ClusterProvider.MINIKUBE);
        response.setStatus(ClusterStatus.CONNECTED);
        return response;
    }
}