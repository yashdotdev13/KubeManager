package com.kubemanager.cluster_service.service;


import com.kubemanager.cluster_service.dto.response.KubernetesEventResponse;

import java.util.UUID;
import java.util.List;

public interface KubernetesEventService {

    List<KubernetesEventResponse> getEvents(
            UUID clusterId,
            String namespace
    );

    List<KubernetesEventResponse> getPodEvents(
            UUID clusterId,
            String namespace,
            String podName
    );
}