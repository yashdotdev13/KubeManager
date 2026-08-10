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
public class SecretResponse {

    private String name;

    private String namespace;

    private String type;

    private Map<String, String> data;

    private OffsetDateTime creationTimestamp;
}
