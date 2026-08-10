package com.kubemanager.cluster_service.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IngressResponse {

    private String name;

    private String namespace;

    private String host;

    private String path;

    private String serviceName;

    private Integer servicePort;

    private String address;

    private OffsetDateTime creationTimestamp;
}
