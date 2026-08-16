package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.request.NodeDrainRequest;
import com.kubemanager.cluster_service.dto.response.NodeDrainResponse;
import com.kubemanager.cluster_service.dto.response.NodeOperationResponse;
import com.kubemanager.cluster_service.dto.response.NodeResponse;
import com.kubemanager.cluster_service.dto.response.NodeSummaryResponse;
import com.kubemanager.cluster_service.service.NodeService;
import com.kubemanager.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clusters/{clusterId}/nodes")
public class NodeController {

    private final NodeService nodeService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<NodeSummaryResponse>> getNodes(
            @PathVariable UUID clusterId
    ) {

        return ApiResponse.success(
                "Nodes fetched successfully.",
                nodeService.getNodes(clusterId)
        );
    }

    @GetMapping("/{nodeName}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<NodeResponse> getNode(
            @PathVariable UUID clusterId,
            @PathVariable String nodeName
    ) {

        return ApiResponse.success(
                "Node fetched successfully.",
                nodeService.getNode(
                        clusterId,
                        nodeName
                )
        );
    }

    @PostMapping("/{nodeName}/cordon")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<NodeOperationResponse> cordonNode(
            @PathVariable UUID clusterId,
            @PathVariable String nodeName
    ) {

        return ApiResponse.success(
                "Node cordoned successfully.",
                nodeService.cordonNode(
                        clusterId,
                        nodeName
                )
        );
    }

    @PostMapping("/{nodeName}/uncordon")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<NodeOperationResponse> uncordonNode(
            @PathVariable UUID clusterId,
            @PathVariable String nodeName
    ) {

        return ApiResponse.success(
                "Node uncordoned successfully.",
                nodeService.uncordonNode(
                        clusterId,
                        nodeName
                 )
        );
    }

    @PostMapping("/{nodeName}/drain")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<NodeDrainResponse> drainNode(
            @PathVariable UUID clusterId,
            @PathVariable String nodeName,
            @Valid @RequestBody(required = false)
            NodeDrainRequest request
    ) {

        return ApiResponse.success(
                "Node drain initiated successfully.",
                nodeService.drainNode(
                        clusterId,
                        nodeName,
                        request
                )
        );
    }
}