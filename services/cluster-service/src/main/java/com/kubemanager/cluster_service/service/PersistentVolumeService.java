package com.kubemanager.cluster_service.service;

import com.kubemanager.cluster_service.dto.request.CreatePersistentVolumeRequest;
import com.kubemanager.cluster_service.dto.response.PersistentVolumeResponse;
import com.kubemanager.cluster_service.dto.response.PersistentVolumeSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface PersistentVolumeService {

    PersistentVolumeResponse createPersistentVolume(
            UUID clusterId,
            CreatePersistentVolumeRequest request
    );

    List<PersistentVolumeSummaryResponse> getPersistentVolumes(
            UUID clusterId
    );

    PersistentVolumeResponse getPersistentVolume(
            UUID clusterId,
            String volumeName
    );

    void deletePersistentVolume(
            UUID clusterId,
            String volumeName
    );
}
