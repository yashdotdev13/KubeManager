package com.kubemanager.cluster_service.service;

import com.kubemanager.cluster_service.dto.request.CreateJobRequest;
import com.kubemanager.cluster_service.dto.response.JobResponse;
import com.kubemanager.cluster_service.dto.response.JobSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface JobService {

    JobResponse createJob(
            UUID clusterId,
            String namespace,
            CreateJobRequest request
    );

    List<JobSummaryResponse> getJobs(
            UUID clusterId,
            String namespace
    );

    JobResponse getJob(
            UUID clusterId,
            String namespace,
            String jobName
    );

    void deleteJob(
            UUID clusterId,
            String namespace,
            String jobName
    );
}
