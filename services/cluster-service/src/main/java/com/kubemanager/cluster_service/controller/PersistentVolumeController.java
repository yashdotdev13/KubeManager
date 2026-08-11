package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.request.CreatePersistentVolumeRequest;
import com.kubemanager.cluster_service.dto.response.PersistentVolumeResponse;
import com.kubemanager.cluster_service.dto.response.PersistentVolumeSummaryResponse;
import com.kubemanager.cluster_service.service.PersistentVolumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clusters/{clusterId}/persistent-volumes")
@RequiredArgsConstructor
public class PersistentVolumeController {

    private final PersistentVolumeService persistentVolumeService;

    @PostMapping
    public ResponseEntity<PersistentVolumeResponse> createPersistentVolume(
            @PathVariable UUID clusterId,
            @Valid @RequestBody CreatePersistentVolumeRequest request
    ) {

        PersistentVolumeResponse response =
                persistentVolumeService.createPersistentVolume(
                        clusterId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<PersistentVolumeSummaryResponse>>
    getPersistentVolumes(
            @PathVariable UUID clusterId
    ) {

        List<PersistentVolumeSummaryResponse> response =
                persistentVolumeService.getPersistentVolumes(
                        clusterId
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{volumeName}")
    public ResponseEntity<PersistentVolumeResponse>
    getPersistentVolume(
            @PathVariable UUID clusterId,
            @PathVariable String volumeName
    ) {

        PersistentVolumeResponse response =
                persistentVolumeService.getPersistentVolume(
                        clusterId,
                        volumeName
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{volumeName}")
    public ResponseEntity<Void> deletePersistentVolume(
            @PathVariable UUID clusterId,
            @PathVariable String volumeName
    ) {

        persistentVolumeService.deletePersistentVolume(
                clusterId,
                volumeName
        );
        return ResponseEntity.noContent().build();
    }
}