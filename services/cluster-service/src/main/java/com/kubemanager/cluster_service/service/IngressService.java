package com.kubemanager.cluster_service.service;

import com.kubemanager.cluster_service.dto.request.CreateIngressRequest;
import com.kubemanager.cluster_service.dto.response.IngressResponse;
import com.kubemanager.cluster_service.dto.response.IngressSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface IngressService {

    IngressResponse createIngress(UUID clusterId, CreateIngressRequest request);

    List<IngressSummaryResponse> getIngresses(UUID clusterId, String namespace);

    IngressResponse getIngress(UUID clusterId, String namespace);

    void deleteIngress(UUID clusterId, String namespace, String ingressName);
}
