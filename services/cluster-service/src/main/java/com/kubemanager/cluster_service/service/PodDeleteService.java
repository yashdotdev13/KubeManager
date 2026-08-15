package com.kubemanager.cluster_service.service;

import com.kubemanager.cluster_service.dto.response.PodDeleteResponse;

import java.util.UUID;

public interface PodDeleteService {

    PodDeleteResponse deletePod(
            UUID clusterId,
            String namespace,
            String podName
    );
}