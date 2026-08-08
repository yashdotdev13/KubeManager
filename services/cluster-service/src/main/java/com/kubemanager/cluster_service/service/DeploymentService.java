package com.kubemanager.cluster_service.service;

import com.kubemanager.cluster_service.dto.request.CreateDeploymentRequest;
import com.kubemanager.cluster_service.dto.request.ScaleDeploymentRequest;
import com.kubemanager.cluster_service.dto.response.DeploymentResponse;
import com.kubemanager.cluster_service.dto.response.DeploymentSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface  DeploymentService {

    List<DeploymentSummaryResponse> getDeployments(UUID clusterId, String namespace);

    DeploymentResponse getDeployment(UUID clusterId, String namespace , String deploymentName);

    DeploymentResponse scaleDeployment(UUID clusterId, String namespace,
                                       String deploymentName,
                                       ScaleDeploymentRequest request);

    void restartDeployment(UUID clusterId, String namespace, String deploymentName);

    void deleteDeployment(UUID clusterId, String namespace, String deploymentName);


    DeploymentResponse createDeployment(UUID clusterId, CreateDeploymentRequest request);
}
