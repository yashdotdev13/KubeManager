package com.kubemanager.cluster_service.service;

import com.kubemanager.cluster_service.dto.request.CreateNamespaceRequest;
import com.kubemanager.cluster_service.dto.response.NamespaceResponse;
import com.kubemanager.cluster_service.dto.response.NamespaceSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface NamespaceService {


    List<NamespaceSummaryResponse> getNamespace(UUID clusterId);

    NamespaceResponse getNamespace(UUID clusterId, String namespace);

    NamespaceResponse createNamespace(UUID clusterId, CreateNamespaceRequest request);

    void deleteNamespace(UUID clusterId, String namespace);
}
