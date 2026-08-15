package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.request.PodExecRequest;
import com.kubemanager.cluster_service.dto.response.PodExecResponse;
import com.kubemanager.cluster_service.service.PodExecService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/clusters/{clusterId}/namespaces/{namespace}/pods"
)
@RequiredArgsConstructor
public class PodExecController {

    private final PodExecService podExecService;

    @PostMapping("/{podName}/exec")
    public ResponseEntity<PodExecResponse> executeCommand(
            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String podName,
            @Valid @RequestBody PodExecRequest request
    ) {

        PodExecResponse response =
                podExecService.executeCommand(
                        clusterId,
                        namespace,
                        podName,
                        request
                );

        return ResponseEntity.ok(response);
    }
}