package com.kubemanager.cluster_service.service.Impl;

import com.kubemanager.cluster_service.dto.request.CreatePersistentVolumeClaimRequest;
import com.kubemanager.cluster_service.dto.response.PersistentVolumeClaimResponse;
import com.kubemanager.cluster_service.dto.response.PersistentVolumeClaimSummaryResponse;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.PersistentVolumeClaimService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersistentVolumeClaimServiceImpl
        implements PersistentVolumeClaimService {

    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;
    private final PersistentVolumeClaimMapper persistentVolumeClaimMapper;

    @Override
    public PersistentVolumeClaimResponse createPersistentVolumeClaim(
            UUID clusterId,
            CreatePersistentVolumeClaimRequest request
    ) {
        return null;
    }

    @Override
    public List<PersistentVolumeClaimSummaryResponse> getPersistentVolumeClaims(
            UUID clusterId,
            String namespace
    ) {
        return List.of();
    }

    @Override
    public PersistentVolumeClaimResponse getPersistentVolumeClaim(
            UUID clusterId,
            String namespace,
            String pvcName
    ) {
        return null;
    }

    @Override
    public void deletePersistentVolumeClaim(
            UUID clusterId,
            String namespace,
            String pvcName
    ) {

    }
}
