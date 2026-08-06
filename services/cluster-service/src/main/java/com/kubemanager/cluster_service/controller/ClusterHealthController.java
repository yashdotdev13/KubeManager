package com.kubemanager.cluster_service.controller;



import com.kubemanager.cluster_service.dto.response.ClusterHealthResponse;
import com.kubemanager.cluster_service.service.ClusterHealthService;
import com.kubemanager.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clusters")
@RequiredArgsConstructor
public class ClusterHealthController {

    private final ClusterHealthService clusterHealthService;

    @PostMapping("/{clusterId}/health-check")
    public ResponseEntity<ApiResponse<ClusterHealthResponse>> healthCheck(
            @PathVariable UUID clusterId
    ) {

        ClusterHealthResponse response =
                clusterHealthService.healthCheck(clusterId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cluster health check completed successfully.",
                        response
                )
        );
    }
}