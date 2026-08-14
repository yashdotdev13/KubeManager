package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.response.PodLogsResponse;
import com.kubemanager.cluster_service.service.PodLogsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/clusters/{clusterId}/namespaces/{namespace}/pods"
)
@RequiredArgsConstructor
public class PodLogsController {

    private final PodLogsService podLogsService;

    @GetMapping("/{podName}/logs")
    public ResponseEntity<PodLogsResponse> getPodLogs(
            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String podName,

            @RequestParam(required = false)
            String container,

            @RequestParam(required = false)
            Integer tailLines,

            @RequestParam(defaultValue = "false")
            Boolean previous
    ) {

        PodLogsResponse response =
                podLogsService.getPodLogs(
                        clusterId,
                        namespace,
                        podName,
                        container,
                        tailLines,
                        previous
                );

        return ResponseEntity.ok(response);
    }
}