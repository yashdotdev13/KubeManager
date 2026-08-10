package com.kubemanager.cluster_service.service;

import com.kubemanager.cluster_service.dto.request.CreateConfigMapRequest;
import com.kubemanager.cluster_service.dto.response.ConfigMapResponse;
import com.kubemanager.cluster_service.dto.response.ConfigMapSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface ConfigMapService {


    ConfigMapResponse createConfigMap(UUID clusterId, CreateConfigMapRequest request);

    List<ConfigMapSummaryResponse> getConfigMaps(UUID clusterId, String namespace);

    ConfigMapResponse getConfigMap(UUID clusterId, String namespace, String configMapName);


    void deleteConfigMap(UUID clusterId, String namespace, String configMapNme);



}
