package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.request.CreateDeploymentRequest;
import com.kubemanager.cluster_service.dto.request.ScaleDeploymentRequest;
import com.kubemanager.cluster_service.dto.response.DeploymentResponse;
import com.kubemanager.cluster_service.dto.response.DeploymentSummaryResponse;
import com.kubemanager.cluster_service.service.DeploymentService;
import com.kubemanager.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clusters/{clusterId}/deployments")
public class DeploymentController {

    private final DeploymentService deploymentService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<DeploymentSummaryResponse>> getDeployments(

            @PathVariable UUID clusterId,

            @RequestParam(required = false)
            String namespace
    ) {

        return ApiResponse.success(
                "Deployments fetched successfully.",
                deploymentService.getDeployments(
                        clusterId,
                        namespace
                )
        );
    }

    @GetMapping("/{namespace}/{deploymentName}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<DeploymentResponse> getDeployment(

            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String deploymentName
    ) {

        return ApiResponse.success(
                "Deployment fetched successfully.",
                deploymentService.getDeployment(
                        clusterId,
                        namespace,
                        deploymentName
                )
        );
    }

    @PutMapping("/{namespace}/{deploymentName}/scale")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<DeploymentResponse> scaleDeployment(

            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String deploymentName,

            @Valid
            @RequestBody
            ScaleDeploymentRequest request
    ) {

        return ApiResponse.success(
                "Deployment scaled successfully.",
                deploymentService.scaleDeployment(
                        clusterId,
                        namespace,
                        deploymentName,
                        request
                )
        );
    }

    @PostMapping("/{namespace}/{deploymentName}/restart")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> restartDeployment(

            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String deploymentName
    ) {

        deploymentService.restartDeployment(
                clusterId,
                namespace,
                deploymentName
        );

        return ApiResponse.success(
                "Deployment restarted successfully.",
                null
        );
    }

    @DeleteMapping("/{namespace}/{deploymentName}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteDeployment(

            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String deploymentName
    ) {

        deploymentService.deleteDeployment(
                clusterId,
                namespace,
                deploymentName
        );

        return ApiResponse.success(
                "Deployment deleted successfully.",
                null
        );
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DeploymentResponse> createDeployment(

            @PathVariable UUID clusterId,

            @Valid
            @RequestBody
            CreateDeploymentRequest request
    ) {

        return ApiResponse.success(
                "Deployment created successfully.",
                deploymentService.createDeployment(
                        clusterId,
                        request
                )
        );
    }
}