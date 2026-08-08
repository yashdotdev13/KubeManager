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
public class ServiceResponse {


    private String name;

    private String namespace;

    private String type;

    private String clusterIP;

    private Integer port;

    private Integer targetPort;

    private Integer nodePort;

    private Map<String, String> selector;

    private OffsetDateTime creationTimestamp;
}
