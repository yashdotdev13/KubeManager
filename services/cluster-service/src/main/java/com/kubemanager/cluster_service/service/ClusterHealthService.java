package com.kubemanager.cluster_service.service;

import com.kubemanager.cluster_service.dto.response.ClusterHealthResponse;

import java.util.UUID;

public interface ClusterHealthService {

    ClusterHealthResponse healthCheck(UUID clusterId);
}
