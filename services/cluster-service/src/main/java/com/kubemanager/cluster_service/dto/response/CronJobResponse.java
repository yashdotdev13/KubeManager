package com.kubemanager.cluster_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CronJobResponse {

    private String name;

    private String namespace;

    private String schedule;

    private Integer completions;

    private Integer parallelism;

    private Integer backoffLimit;

    private String containerName;

    private String image;

    private Map<String, String> labels;

    private Map<String, String> environment;

    private OffsetDateTime creationTimestamp;

    private Boolean suspend;

    private Integer successfulJobsHistoryLimit;

    private Integer failedJobsHistoryLimit;
}