package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.response.PodResponse;
import com.kubemanager.cluster_service.dto.response.PodSummaryResponse;
import com.kubemanager.cluster_service.service.PodService;
import com.kubemanager.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clusters/{clusterId}/pods")
public class PodController {

    private final PodService podService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<PodSummaryResponse>> getPods(

            @PathVariable UUID clusterId,

            @RequestParam(required = false)
            String namespace
    ) {

        return ApiResponse.success(
                "Pods fetched successfully.",
                podService.getPods(
                        clusterId,
                        namespace
                )
        );
    }

    @GetMapping("/{namespace}/{podName}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PodResponse> getPod(

            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String podName
    ) {

        return ApiResponse.success(
                "Pod fetched successfully.",
                podService.getPod(
                        clusterId,
                        namespace,
                        podName
                )
        );
    }

    @DeleteMapping("/{namespace}/{podName}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deletePod(

            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String podName
    ) {

        podService.deletePod(
                clusterId,
                namespace,
                podName
        );

        return ApiResponse.success(
                "Pod deleted successfully.",
                null
        );
    }
}