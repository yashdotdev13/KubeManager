package com.kubemanager.ai_service.dto;

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
public class PodResponse {

    private String name;

    private String namespace;

    private String status;

    private String node;

    private String podIp;

    private String hostIp;

    private String qosClass;

    private String serviceAccount;

    private OffsetDateTime startTime;

    private Map<String, String> labels;

    private Map<String, String> annotations;
}