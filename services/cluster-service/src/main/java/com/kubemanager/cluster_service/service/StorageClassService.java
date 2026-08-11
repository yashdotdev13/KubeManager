package com.kubemanager.cluster_service.service;


import com.kubemanager.cluster_service.dto.request.CreateStorageClassRequest;
import com.kubemanager.cluster_service.dto.response.StorageClassResponse;
import com.kubemanager.cluster_service.dto.response.StorageClassSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface StorageClassService {

    StorageClassResponse createStorageClass(
            UUID clusterId,
            CreateStorageClassRequest request
    );

    List<StorageClassSummaryResponse> getStorageClasses(
            UUID clusterId
    );

    StorageClassResponse getStorageClass(
            UUID clusterId,
            String storageClassName
    );

    void deleteStorageClass(
            UUID clusterId,
            String storageClassName
    );
}
