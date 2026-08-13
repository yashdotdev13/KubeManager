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
public class StatefulSetResponse {

    private String name;

    private String namespace;

    private Integer desiredReplicas;

    private Integer currentReplicas;

    private Integer readyReplicas;

    private String serviceName;

    private String containerName;

    private String image;

    private Integer containerPort;

    private Map<String, String> labels;

    private Map<String, String> environment;

    private OffsetDateTime creationTimestamp;
}