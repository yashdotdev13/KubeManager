package com.kubemanager.cluster_service.service;



import com.kubemanager.cluster_service.dto.response.PodEventResponse;

import java.util.List;
import java.util.UUID;

public interface PodEventService {

    List<PodEventResponse> getPodEvents(
            UUID clusterId,
            String namespace,
            String podName
    );
}