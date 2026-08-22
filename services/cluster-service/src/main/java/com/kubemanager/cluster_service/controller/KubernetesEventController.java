package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.response.KubernetesEventResponse;
import com.kubemanager.cluster_service.service.KubernetesEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clusters/{clusterId}/events")
@RequiredArgsConstructor
public class KubernetesEventController {

    private final KubernetesEventService kubernetesEventService;

    @GetMapping("/{namespace}")
    public ResponseEntity<List<KubernetesEventResponse>> getEvents(
            @PathVariable UUID clusterId,
            @PathVariable String namespace
    ) {

        return ResponseEntity.ok(
                kubernetesEventService.getEvents(
                        clusterId,
                        namespace
                )
        );
    }

    @GetMapping("/{namespace}/{podName}")
    public ResponseEntity<List<KubernetesEventResponse>> getPodEvents(
            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String podName
    ) {

        return ResponseEntity.ok(
                kubernetesEventService.getPodEvents(
                        clusterId,
                        namespace,
                        podName
                )
        );
    }
}