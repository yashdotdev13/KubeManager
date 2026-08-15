package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.response.PodRestartResponse;
import com.kubemanager.cluster_service.service.PodRestartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/clusters/{clusterId}/namespaces/{namespace}/pods"
)
@RequiredArgsConstructor
public class PodRestartController {

    private final PodRestartService podRestartService;

    @PostMapping("/{podName}/restart")
    public ResponseEntity<PodRestartResponse> restartPod(
            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String podName
    ) {

        PodRestartResponse response =
                podRestartService.restartPod(
                        clusterId,
                        namespace,
                        podName
                );

        return ResponseEntity.ok(response);
    }
}