package com.kubemanager.cluster_service.controller;


import com.kubemanager.cluster_service.dto.request.CreateReplicaSetRequest;
import com.kubemanager.cluster_service.dto.request.ReplicaSetResponse;
import com.kubemanager.cluster_service.dto.request.ReplicaSetSummaryResponse;
import com.kubemanager.cluster_service.service.ReplicaSetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/clusters/{clusterId}/namespaces/{namespace}/replicasets"
)
@RequiredArgsConstructor
public class ReplicaSetController {

    private final ReplicaSetService replicaSetService;

    @PostMapping
    public ResponseEntity<ReplicaSetResponse> createReplicaSet(
            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @Valid @RequestBody CreateReplicaSetRequest request
    ) {

        ReplicaSetResponse response =
                replicaSetService.createReplicaSet(
                        clusterId,
                        namespace,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReplicaSetSummaryResponse>> getReplicaSets(
            @PathVariable UUID clusterId,
            @PathVariable String namespace
    ) {

        List<ReplicaSetSummaryResponse> response =
                replicaSetService.getReplicaSets(
                        clusterId,
                        namespace
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{replicaSetName}")
    public ResponseEntity<ReplicaSetResponse> getReplicaSet(
            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String replicaSetName
    ) {

        ReplicaSetResponse response =
                replicaSetService.getReplicaSet(
                        clusterId,
                        namespace,
                        replicaSetName
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{replicaSetName}")
    public ResponseEntity<Void> deleteReplicaSet(
            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String replicaSetName
    ) {

        replicaSetService.deleteReplicaSet(
                clusterId,
                namespace,
                replicaSetName
        );
        return ResponseEntity.noContent().build();
    }
}