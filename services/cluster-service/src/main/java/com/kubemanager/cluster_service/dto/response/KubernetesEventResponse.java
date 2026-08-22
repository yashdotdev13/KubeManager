package com.kubemanager.cluster_service.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class KubernetesEventResponse {

    private String name;
    private String namespace;
    private String type;
    private String reason;
    private String message;
    private String involvedKind;
    private String involvedName;
    private String source;
    private Integer count;

    private OffsetDateTime firstTimestamp;
    private OffsetDateTime lastTimestamp;
}