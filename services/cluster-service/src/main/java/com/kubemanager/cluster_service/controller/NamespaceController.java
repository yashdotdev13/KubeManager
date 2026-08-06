package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.request.CreateNamespaceRequest;
import com.kubemanager.cluster_service.dto.response.NamespaceResponse;
import com.kubemanager.cluster_service.dto.response.NamespaceSummaryResponse;
import com.kubemanager.cluster_service.service.NamespaceService;
import com.kubemanager.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clusters/{clusterId}/namespaces")
public class NamespaceController {

    private final NamespaceService namespaceService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<NamespaceSummaryResponse>> getNamespaces(
            @PathVariable UUID clusterId
    ) {

        return ApiResponse.success(
                "Namespaces fetched successfully.",
                namespaceService.getNamespaces(clusterId)
        );
    }

    @GetMapping("/{namespace}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<NamespaceResponse> getNamespace(
            @PathVariable UUID clusterId,
            @PathVariable String namespace
    ) {

        return ApiResponse.success(
                "Namespace fetched successfully.",
                namespaceService.getNamespace(
                        clusterId,
                        namespace
                )
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<NamespaceResponse> createNamespace(
            @PathVariable UUID clusterId,
            @Valid @RequestBody CreateNamespaceRequest request
    ) {

        return ApiResponse.success(
                "Namespace created successfully.",
                namespaceService.createNamespace(
                        clusterId,
                        request
                )
        );
    }

    @DeleteMapping("/{namespace}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteNamespace(
            @PathVariable UUID clusterId,
            @PathVariable String namespace
    ) {

        namespaceService.deleteNamespace(
                clusterId,
                namespace
        );

        return ApiResponse.success(
                "Namespace deleted successfully.",
                null
        );
    }
}