package com.kubemanager.cluster_service.service;

import com.kubemanager.cluster_service.dto.response.NodeOperationResponse;
import com.kubemanager.cluster_service.dto.response.NodeResponse;
import com.kubemanager.cluster_service.dto.response.NodeSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface NodeService {

    List<NodeSummaryResponse> getNodes(UUID clusterId);

    NodeResponse getNode(UUID clusterId, String nodeName);

    NodeOperationResponse cordonNode(UUID clusterId, String nodeName);

    NodeOperationResponse uncordonNode(
            UUID clusterId,
            String nodeName
    );
}
