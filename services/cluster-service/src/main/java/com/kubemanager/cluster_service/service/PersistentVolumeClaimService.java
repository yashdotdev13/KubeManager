package com.kubemanager.cluster_service.service;

import com.kubemanager.cluster_service.dto.request.CreatePersistentVolumeClaimRequest;
import com.kubemanager.cluster_service.dto.response.PersistentVolumeClaimResponse;
import com.kubemanager.cluster_service.dto.response.PersistentVolumeClaimSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface PersistentVolumeClaimService {


    PersistentVolumeClaimResponse createPersistentVolumeClaim(
            UUID clusterId,
            CreatePersistentVolumeClaimRequest request
    );

    List<PersistentVolumeClaimSummaryResponse> getPersistentVolumeClaims(
            UUID clusterId,
            String namespace
    );

    PersistentVolumeClaimResponse getPersistentVolumeClaim(
            UUID clusterId,
            String namespace,
            String pvcName
    );

    void deletePersistentVolumeClaim(
            UUID clusterId,
            String namespace,
            String pvcName
    );
}
