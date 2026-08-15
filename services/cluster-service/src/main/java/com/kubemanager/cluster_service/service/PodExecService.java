package com.kubemanager.cluster_service.service;

import com.kubemanager.cluster_service.dto.request.PodExecRequest;
import com.kubemanager.cluster_service.dto.response.PodExecResponse;

import java.util.UUID;

public interface PodExecService {

    PodExecResponse executeCommand(
            UUID clusterId,
            String namespace,
            String podName,
            PodExecRequest request
    );
}