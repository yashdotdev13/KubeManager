package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.response.PodDeleteResponse;
import com.kubemanager.cluster_service.service.PodDeleteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/clusters/{clusterId}/namespaces/{namespace}/pods"
)
@RequiredArgsConstructor
public class PodDeleteController {

    private final PodDeleteService podDeleteService;

    @DeleteMapping("/{podName}")
    public ResponseEntity<PodDeleteResponse> deletePod(
            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String podName
    ) {

        PodDeleteResponse response =
                podDeleteService.deletePod(
                        clusterId,
                        namespace,
                        podName
                );

        return ResponseEntity.ok(response);
    }
}