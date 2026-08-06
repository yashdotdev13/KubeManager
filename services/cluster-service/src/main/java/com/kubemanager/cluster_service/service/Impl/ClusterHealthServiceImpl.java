package com.kubemanager.cluster_service.service.Impl;


import com.kubemanager.cluster_service.dto.response.ClusterHealthResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.service.KubernetesConnectionService;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.ClusterHealthService;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClusterHealthServiceImpl implements ClusterHealthService {

    private final ClusterRepository clusterRepository;
    private final KubernetesConnectionService kubernetesConnectionService;



    @Override
    public ClusterHealthResponse healthCheck(UUID clusterId) {


        log.info("Performing health cheeck for cluster: {}",clusterId);

        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(()->new ResourceNotFoundException(
                        ErrorCode.CLUSTER_NOT_FOUND,
                        "Cluster not found with id"+clusterId
                ));


        return  null;
    }
}
