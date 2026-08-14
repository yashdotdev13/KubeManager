package com.kubemanager.cluster_service.service;


import com.kubemanager.cluster_service.dto.request.CreateReplicaSetRequest;
import com.kubemanager.cluster_service.dto.request.ReplicaSetResponse;
import com.kubemanager.cluster_service.dto.request.ReplicaSetSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface ReplicaSetService {

    ReplicaSetResponse createReplicaSet(
            UUID clusterId,
            String namespace,
            CreateReplicaSetRequest request
    );

    List<ReplicaSetSummaryResponse> getReplicaSets(
            UUID clusterId,
            String namespace
    );

    ReplicaSetResponse getReplicaSet(
            UUID clusterId,
            String namespace,
            String replicaSetName
    );

    void deleteReplicaSet(
            UUID clusterId,
            String namespace,
            String replicaSetName
    );
}