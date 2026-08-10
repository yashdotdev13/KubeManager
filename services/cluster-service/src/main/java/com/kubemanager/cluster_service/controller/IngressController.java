package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.request.CreateIngressRequest;
import com.kubemanager.cluster_service.dto.response.IngressResponse;
import com.kubemanager.cluster_service.dto.response.IngressSummaryResponse;
import com.kubemanager.cluster_service.service.IngressService;
import com.kubemanager.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clusters/{clusterId}/ingresses")
public class IngressController {

    private final IngressService ingressService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<IngressResponse> createIngress(

            @PathVariable UUID clusterId,

            @Valid
            @RequestBody
            CreateIngressRequest request
    ) {

        return ApiResponse.success(
                "Ingress created successfully.",
                ingressService.createIngress(
                        clusterId,
                        request
                )
        );
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<IngressSummaryResponse>> getIngresses(

            @PathVariable UUID clusterId,

            @RequestParam(required = false)
            String namespace
    ) {

        return ApiResponse.success(
                "Ingresses fetched successfully.",
                ingressService.getIngresses(
                        clusterId,
                        namespace
                )
        );
    }

    @GetMapping("/{namespace}/{ingressName}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<IngressResponse> getIngress(

            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String ingressName
    ) {

        return ApiResponse.success(
                "Ingress fetched successfully.",
                ingressService.getIngress(
                        clusterId,
                        namespace,
                        ingressName
                )
        );
    }

    @DeleteMapping("/{namespace}/{ingressName}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteIngress(

            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String ingressName
    ) {

        ingressService.deleteIngress(
                clusterId,
                namespace,
                ingressName
        );
        return ApiResponse.success(
                "Ingress deleted successfully.",
                null
        );
    }
}