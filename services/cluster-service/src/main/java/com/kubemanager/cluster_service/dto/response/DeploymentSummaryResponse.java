package com.kubemanager.cluster_service.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeploymentSummaryResponse {

    private String name;

    private String namespace;

    private Integer replicas;

    private Integer readReplicas;

    private Integer availableReplicas;

    private String strategy;
}
