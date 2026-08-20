package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.request.CreateClusterRequest;
import com.kubemanager.cluster_service.dto.request.UpdateClusterRequest;
import com.kubemanager.cluster_service.dto.response.ClusterResponse;
import com.kubemanager.cluster_service.dto.response.ClusterSummaryResponse;
import com.kubemanager.cluster_service.dto.response.NodeResponse;
import com.kubemanager.cluster_service.service.ClusterService;
import com.kubemanager.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clusters")
public class ClusterController {

    private final ClusterService clusterService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ClusterResponse> createCluster(
            @Valid @RequestBody CreateClusterRequest request
    ) {

        return ApiResponse.success(
                "Cluster created successfully.",
                clusterService.createCluster(request)
        );
    }

    @GetMapping
    public ApiResponse<List<ClusterSummaryResponse>> getMyClusters() {

        return ApiResponse.success(
                "Clusters fetched successfully.",
                clusterService.getMyClusters()
        );
    }

    @GetMapping("/{clusterId}")
    public ApiResponse<ClusterResponse> getClusterById(
            @PathVariable UUID clusterId
    ) {

        return ApiResponse.success(
                "Cluster fetched successfully.",
                clusterService.getClusterById(clusterId)
        );
    }

    @PutMapping("/{clusterId}")
    public ApiResponse<ClusterResponse> updateCluster(

            @PathVariable("clusterId") UUID clusterId,

            @Valid
            @RequestBody
            UpdateClusterRequest request
    ) {

        return ApiResponse.success(
                "Cluster updated successfully.",
                clusterService.updateCluster(clusterId, request)
        );
    }

    @DeleteMapping("/{clusterId}")
    public ApiResponse<Void> deleteCluster(
            @PathVariable("clusterId") UUID clusterId
    ) {

        clusterService.deleteCluster(clusterId);

        return ApiResponse.success(
                "Cluster deleted successfully.",
                null
        );
    }


    @PostMapping(
            value = "/{clusterId}/connect",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ClusterResponse> connectCluster(

            @PathVariable("clusterId")
            UUID clusterId,

            @RequestPart("kubeConfig")
            MultipartFile kubeConfig
    ) {

        return ApiResponse.success(
                "Cluster connected successfully.",
                clusterService.connectCluster(
                        clusterId,
                        kubeConfig
                )
        );
    }


    @GetMapping("/{clusterId}/nodes")
    public ApiResponse<List<NodeResponse>> getNodes(
            @PathVariable UUID clusterId
    ) {

        return ApiResponse.success(
                "Nodes fetched successfully.",
                clusterService.getNodes(clusterId)
        );
    }
}