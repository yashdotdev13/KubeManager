package com.kubemanager.cluster_service.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentResponse {

    private String name;

    private String namespace;

    private Integer replicas;

    private Integer readyReplicas;

    private Integer availableReplicas;

    private Integer updatedReplicas;

    private Integer unavailableReplicas;

    private String strategy;

    private OffsetDateTime creationTimestamp;

    private Map<String, String> labels;

    private Map<String, String> annotations;
}
