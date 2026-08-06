package com.kubemanager.cluster_service.controller;


import com.kubemanager.cluster_service.dto.response.ClusterHealthResponse;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.service.ClusterHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cluster/health-check")
@RequiredArgsConstructor
public class ClusterHealthController {

    private final ClusterHealthService clusterHealthService;
    private final KubernetesClientFactory kubernetesClientFactory;

    @GetMapping
    public ClusterHealthResponse healthResponse(@PathVariable UUID clusterId){
        return clusterHealthService.healthCheck(clusterId);

    }



}
