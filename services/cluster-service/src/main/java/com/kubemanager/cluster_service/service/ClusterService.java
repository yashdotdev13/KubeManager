package com.kubemanager.cluster_service.service;

import com.kubemanager.cluster_service.dto.request.CreateClusterRequest;
import com.kubemanager.cluster_service.dto.request.UpdateClusterRequest;
import com.kubemanager.cluster_service.dto.response.ClusterResponse;
import com.kubemanager.cluster_service.dto.response.ClusterSummaryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ClusterService {


    ClusterResponse createCluster(CreateClusterRequest request);


    List<ClusterSummaryResponse> getMyClusters();

    ClusterResponse getClusterById(UUID clusterId);


    ClusterResponse updateCluster(UUID clusterId, UpdateClusterRequest request);

    void deleteCluster(UUID clusterId);

    ClusterResponse connectCluster(UUID clusterId, MultipartFile kubeConfig);
}
