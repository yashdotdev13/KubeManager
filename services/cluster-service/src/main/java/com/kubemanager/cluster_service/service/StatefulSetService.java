package com.kubemanager.cluster_service.service;

import com.kubemanager.cluster_service.dto.request.CreateStatefulSetRequest;
import com.kubemanager.cluster_service.dto.response.StatefulSetResponse;
import com.kubemanager.cluster_service.dto.response.StatefulSetSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface StatefulSetService {

    StatefulSetResponse createStatefulSet(
            UUID clusterId,
            String namespace,
            CreateStatefulSetRequest request
    );

    List<StatefulSetSummaryResponse> getStatefulSets(
            UUID clusterId,
            String namespace
    );

    StatefulSetResponse getStatefulSet(
            UUID clusterId,
            String namespace,
            String statefulSetName
    );

    void deleteStatefulSet(
            UUID clusterId,
            String namespace,
            String statefulSetName
    );
}