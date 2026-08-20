package com.kubemanager.ai_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentInfoResponse {

    private String name;

    private String namespace;

    private String status;

    private Integer replicas;

    private Integer readyReplicas;

    private Integer availableReplicas;

    private Integer updatedReplicas;

    private Integer desiredReplicas;

    private String strategy;

    private Map<String, String> labels;

    private Map<String, String> annotations;
}