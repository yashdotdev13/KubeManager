package com.kubemanager.cluster_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplicaSetSummaryResponse {

    private String name;

    private Integer desiredReplicas;

    private Integer currentReplicas;

    private Integer readyReplicas;

    private Integer availableReplicas;

    private String status;
}