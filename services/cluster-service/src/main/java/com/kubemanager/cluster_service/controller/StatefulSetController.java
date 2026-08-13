package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.request.CreateStatefulSetRequest;
import com.kubemanager.cluster_service.dto.response.StatefulSetResponse;
import com.kubemanager.cluster_service.dto.response.StatefulSetSummaryResponse;
import com.kubemanager.cluster_service.service.StatefulSetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/clusters/{clusterId}/namespaces/{namespace}/statefulsets"
)
@RequiredArgsConstructor
public class StatefulSetController {

    private final StatefulSetService statefulSetService;

    @PostMapping
    public ResponseEntity<StatefulSetResponse> createStatefulSet(
            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @Valid @RequestBody CreateStatefulSetRequest request
    ) {

        StatefulSetResponse response =
                statefulSetService.createStatefulSet(
                        clusterId,
                        namespace,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<StatefulSetSummaryResponse>>
    getStatefulSets(
            @PathVariable UUID clusterId,
            @PathVariable String namespace
    ) {

        List<StatefulSetSummaryResponse> response =
                statefulSetService.getStatefulSets(
                        clusterId,
                        namespace
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{statefulSetName}")
    public ResponseEntity<StatefulSetResponse>
    getStatefulSet(
            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String statefulSetName
    ) {

        StatefulSetResponse response =
                statefulSetService.getStatefulSet(
                        clusterId,
                        namespace,
                        statefulSetName
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{statefulSetName}")
    public ResponseEntity<Void> deleteStatefulSet(
            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String statefulSetName
    ) {

        statefulSetService.deleteStatefulSet(
                clusterId,
                namespace,
                statefulSetName
        );

        return ResponseEntity.noContent().build();
    }
}