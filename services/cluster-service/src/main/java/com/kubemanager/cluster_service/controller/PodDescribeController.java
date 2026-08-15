package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.response.PodDescribeResponse;
import com.kubemanager.cluster_service.service.PodDescribeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/clusters/{clusterId}/namespaces/{namespace}/pods"
)
@RequiredArgsConstructor
public class PodDescribeController {

    private final PodDescribeService podDescribeService;

    @GetMapping("/{podName}/describe")
    public ResponseEntity<PodDescribeResponse> describePod(
            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String podName
    ) {

        PodDescribeResponse response =
                podDescribeService.describePod(
                        clusterId,
                        namespace,
                        podName
                );
        return ResponseEntity.ok(response);
    }
}