package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.request.CreateConfigMapRequest;
import com.kubemanager.cluster_service.dto.response.ConfigMapResponse;
import com.kubemanager.cluster_service.dto.response.ConfigMapSummaryResponse;
import com.kubemanager.cluster_service.service.ConfigMapService;
import com.kubemanager.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clusters/{clusterId}/configmaps")
public class ConfigMapController {

    private final ConfigMapService configMapService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ConfigMapResponse> createConfigMap(

            @PathVariable UUID clusterId,

            @Valid
            @RequestBody
            CreateConfigMapRequest request
    ) {

        return ApiResponse.success(
                "ConfigMap created successfully.",
                configMapService.createConfigMap(
                        clusterId,
                        request
                )
        );
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<ConfigMapSummaryResponse>> getConfigMaps(

            @PathVariable UUID clusterId,

            @RequestParam(required = false)
            String namespace
    ) {

        return ApiResponse.success(
                "ConfigMaps fetched successfully.",
                configMapService.getConfigMaps(
                        clusterId,
                        namespace
                )
        );
    }

    @GetMapping("/{namespace}/{configMapName}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ConfigMapResponse> getConfigMap(

            @PathVariable UUID clusterId,

            @PathVariable String namespace,

            @PathVariable String configMapName
    ) {

        return ApiResponse.success(
                "ConfigMap fetched successfully.",
                configMapService.getConfigMap(
                        clusterId,
                        namespace,
                        configMapName
                )
        );
    }

    @DeleteMapping("/{namespace}/{configMapName}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteConfigMap(

            @PathVariable UUID clusterId,

            @PathVariable String namespace,

            @PathVariable String configMapName
    ) {

        configMapService.deleteConfigMap(
                clusterId,
                namespace,
                configMapName
        );

        return ApiResponse.success(
                "ConfigMap deleted successfully.",
                null
        );
    }
}
