package com.kubemanager.cluster_service.controller;

import com.kubemanager.cluster_service.dto.request.CreateJobRequest;
import com.kubemanager.cluster_service.dto.response.JobResponse;
import com.kubemanager.cluster_service.dto.response.JobSummaryResponse;
import com.kubemanager.cluster_service.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/clusters/{clusterId}/namespaces/{namespace}/jobs"
)
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @Valid @RequestBody CreateJobRequest request
    ) {

        JobResponse response = jobService.createJob(
                clusterId,
                namespace,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<JobSummaryResponse>> getJobs(
            @PathVariable UUID clusterId,
            @PathVariable String namespace
    ) {

        List<JobSummaryResponse> response =
                jobService.getJobs(
                        clusterId,
                        namespace
                );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{jobName}")
    public ResponseEntity<JobResponse> getJob(
            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String jobName
    ) {

        JobResponse response =
                jobService.getJob(
                        clusterId,
                        namespace,
                        jobName
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{jobName}")
    public ResponseEntity<Void> deleteJob(
            @PathVariable UUID clusterId,
            @PathVariable String namespace,
            @PathVariable String jobName
    ) {

        jobService.deleteJob(
                clusterId,
                namespace,
                jobName
        );

        return ResponseEntity.noContent().build();
    }
}