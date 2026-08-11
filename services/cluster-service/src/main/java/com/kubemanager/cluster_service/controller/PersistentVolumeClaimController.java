package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.request.CreatePersistentVolumeClaimRequest;
import com.kubemanager.cluster_service.dto.response.PersistentVolumeClaimResponse;
import com.kubemanager.cluster_service.dto.response.PersistentVolumeClaimSummaryResponse;
import com.kubemanager.cluster_service.service.PersistentVolumeClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clusters/{clusterId}/persistent-volume-claims")
@RequiredArgsConstructor
public class PersistentVolumeClaimController {

    private final PersistentVolumeClaimService persistentVolumeClaimService;

    @PostMapping
    public ResponseEntity<PersistentVolumeClaimResponse> createPersistentVolumeClaim(
            @PathVariable UUID clusterId,
            @Valid @RequestBody CreatePersistentVolumeClaimRequest request
    ) {

        PersistentVolumeClaimResponse response =
                persistentVolumeClaimService.createPersistentVolumeClaim(
                        clusterId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<PersistentVolumeClaimSummaryResponse>>
    getPersistentVolumeClaims(
            @PathVariable UUID clusterId,
            @RequestParam(required = false) String namespace
    ) {

        List<PersistentVolumeClaimSummaryResponse> response =
                persistentVolumeClaimService.getPersistentVolumeClaims(
                        clusterId,
                        namespace
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{pvcName}")
    public ResponseEntity<PersistentVolumeClaimResponse>
    getPersistentVolumeClaim(
            @PathVariable UUID clusterId,
            @PathVariable String pvcName,
            @RequestParam String namespace
    ) {

        PersistentVolumeClaimResponse response =
                persistentVolumeClaimService.getPersistentVolumeClaim(
                        clusterId,
                        namespace,
                        pvcName
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{pvcName}")
    public ResponseEntity<Void> deletePersistentVolumeClaim(
            @PathVariable UUID clusterId,
            @PathVariable String pvcName,
            @RequestParam String namespace
    ) {

        persistentVolumeClaimService.deletePersistentVolumeClaim(
                clusterId,
                namespace,
                pvcName
        );

        return ResponseEntity.noContent().build();
    }
}