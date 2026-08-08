package com.kubemanager.cluster_service.service;

import com.kubemanager.cluster_service.dto.response.PodResponse;
import com.kubemanager.cluster_service.dto.response.PodSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface PodService {

    List<PodSummaryResponse> getPods(UUID clusterId,String namespace);

    PodResponse getPod(UUID clusterId, String namespace, String podName);


    void deletePod(UUID clusterId, String namespace, String podName);
}
