package com.kubemanager.cluster_service.service;

import com.kubemanager.cluster_service.dto.request.CreateServiceRequest;
import com.kubemanager.cluster_service.dto.response.ServiceResponse;
import com.kubemanager.cluster_service.dto.response.ServiceSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface ServiceService {

    ServiceResponse createService(UUID clusterId, CreateServiceRequest request);

    List<ServiceSummaryResponse> getServices(UUID clusterId, String namespace);

    ServiceResponse getService(UUID clusterId, String namespace, String serviceName);

    void deleteService(UUID clusterId, String namespace, String serviceName);

}
