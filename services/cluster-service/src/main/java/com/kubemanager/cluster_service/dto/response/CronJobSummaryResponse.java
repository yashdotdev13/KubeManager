package com.kubemanager.cluster_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CronJobSummaryResponse {

    private String name;

    private String schedule;

    private Boolean suspend;

    private String status;

    private String lastScheduleTime;
}