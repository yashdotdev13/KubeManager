package com.kubemanager.ai_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentSummaryResponse {

    private String name;

    private String namespace;

    private String status;

    private Integer replicas;

    private Integer availableReplicas;

    private Integer readyReplicas;

    private Integer updatedReplicas;
}