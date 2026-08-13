package com.kubemanager.cluster_service.service;

import com.kubemanager.cluster_service.dto.request.CreateCronJobRequest;
import com.kubemanager.cluster_service.dto.response.CronJobResponse;
import com.kubemanager.cluster_service.dto.response.CronJobSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface CronJobService {

    CronJobResponse createCronJob(
            UUID clusterId,
            String namespace,
            CreateCronJobRequest request
    );

    List<CronJobSummaryResponse> getCronJobs(
            UUID clusterId,
            String namespace
    );

    CronJobResponse getCronJob(
            UUID clusterId,
            String namespace,
            String cronJobName
    );

    void deleteCronJob(
            UUID clusterId,
            String namespace,
            String cronJobName
    );
}