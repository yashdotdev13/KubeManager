package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.request.CreateServiceRequest;
import com.kubemanager.cluster_service.dto.response.ServiceResponse;
import com.kubemanager.cluster_service.dto.response.ServiceSummaryResponse;
import com.kubemanager.cluster_service.service.ServiceService;
import com.kubemanager.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clusters/{clusterId}/services")
public class ServiceController {

    private final ServiceService serviceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ServiceResponse> createService(

            @PathVariable UUID clusterId,

            @Valid
            @RequestBody
            CreateServiceRequest request
    ) {

        return ApiResponse.success(
                "Service created successfully.",
                serviceService.createService(
                        clusterId,
                        request
                )
        );
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<ServiceSummaryResponse>> getServices(

            @PathVariable UUID clusterId,

            @RequestParam(required = false)
            String namespace
    ) {

        return ApiResponse.success(
                "Services fetched successfully.",
                serviceService.getServices(
                        clusterId,
                        namespace
                )
        );
    }

    @GetMapping("/{namespace}/{serviceName}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<ServiceResponse> getService(

            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String serviceName
    ) {

        return ApiResponse.success(
                "Service fetched successfully.",
                serviceService.getService(
                        clusterId,
                        namespace,
                        serviceName
                )
        );
    }

    @DeleteMapping("/{namespace}/{serviceName}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteService(

            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String serviceName
    ) {

        serviceService.deleteService(
                clusterId,
                namespace,
                serviceName
        );

        return ApiResponse.success(
                "Service deleted successfully.",
                null
        );
    }
}
