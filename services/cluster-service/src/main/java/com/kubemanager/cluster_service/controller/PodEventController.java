package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.response.PodEventResponse;
import com.kubemanager.cluster_service.service.PodEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/clusters/{clusterId}/namespaces/{namespace}/pods"
)
@RequiredArgsConstructor
public class PodEventController {

    private final PodEventService podEventService;

    @GetMapping("/{podName}/events")
    public ResponseEntity<List<PodEventResponse>> getPodEvents(
            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String podName
    ) {

        List<PodEventResponse> response =
                podEventService.getPodEvents(
                        clusterId,
                        namespace,
                        podName
                );
        return ResponseEntity.ok(response);
    }
}