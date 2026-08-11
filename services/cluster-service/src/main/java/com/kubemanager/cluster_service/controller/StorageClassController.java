package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.request.CreateStorageClassRequest;
import com.kubemanager.cluster_service.dto.response.StorageClassResponse;
import com.kubemanager.cluster_service.dto.response.StorageClassSummaryResponse;
import com.kubemanager.cluster_service.service.StorageClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clusters/{clusterId}/storage-classes")
@RequiredArgsConstructor
public class StorageClassController {

    private final StorageClassService storageClassService;

    @PostMapping
    public ResponseEntity<StorageClassResponse> createStorageClass(
            @PathVariable UUID clusterId,
            @Valid @RequestBody CreateStorageClassRequest request
    ) {

        StorageClassResponse response =
                storageClassService.createStorageClass(
                        clusterId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<StorageClassSummaryResponse>>
    getStorageClasses(
            @PathVariable UUID clusterId
    ) {

        List<StorageClassSummaryResponse> response =
                storageClassService.getStorageClasses(
                        clusterId
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{storageClassName}")
    public ResponseEntity<StorageClassResponse>
    getStorageClass(
            @PathVariable UUID clusterId,
            @PathVariable String storageClassName
    ) {

        StorageClassResponse response =
                storageClassService.getStorageClass(
                        clusterId,
                        storageClassName
                );

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{storageClassName}")
    public ResponseEntity<Void> deleteStorageClass(
            @PathVariable UUID clusterId,
            @PathVariable String storageClassName
    ) {

        storageClassService.deleteStorageClass(
                clusterId,
                storageClassName
        );

        return ResponseEntity.noContent().build();
    }
}