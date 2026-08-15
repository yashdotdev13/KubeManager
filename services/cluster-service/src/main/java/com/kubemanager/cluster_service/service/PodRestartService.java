package com.kubemanager.cluster_service.service;

import com.kubemanager.cluster_service.dto.response.PodRestartResponse;

import java.util.UUID;

public interface PodRestartService {

    PodRestartResponse restartPod(
            UUID clusterId,
            String namespace,
            String podName
    );
}