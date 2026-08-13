package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.request.CreateCronJobRequest;
import com.kubemanager.cluster_service.dto.response.CronJobResponse;
import com.kubemanager.cluster_service.dto.response.CronJobSummaryResponse;
import com.kubemanager.cluster_service.service.CronJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/clusters/{clusterId}/namespaces/{namespace}/cronjobs"
)
@RequiredArgsConstructor
public class CronJobController {

    private final CronJobService cronJobService;

    @PostMapping
    public ResponseEntity<CronJobResponse> createCronJob(
            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @Valid @RequestBody CreateCronJobRequest request
    ) {

        CronJobResponse response =
                cronJobService.createCronJob(
                        clusterId,
                        namespace,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<CronJobSummaryResponse>> getCronJobs(
            @PathVariable UUID clusterId,
            @PathVariable String namespace
    ) {

        List<CronJobSummaryResponse> response =
                cronJobService.getCronJobs(
                        clusterId,
                        namespace
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{cronJobName}")
    public ResponseEntity<CronJobResponse> getCronJob(
            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String cronJobName
    ) {

        CronJobResponse response =
                cronJobService.getCronJob(
                        clusterId,
                        namespace,
                        cronJobName
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{cronJobName}")
    public ResponseEntity<Void> deleteCronJob(
            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String cronJobName
    ) {

        cronJobService.deleteCronJob(
                clusterId,
                namespace,
                cronJobName
        );

        return ResponseEntity.noContent().build();
    }
}