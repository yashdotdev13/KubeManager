package com.kubemanager.cluster_service.service;

import com.kubemanager.cluster_service.dto.response.PodLogsResponse;

import java.util.UUID;

public interface PodLogsService {

    PodLogsResponse getPodLogs(
            UUID clusterId,
            String namespace,
            String podName,
            String container,
            Integer tailLines,
            Boolean previous
    );
}