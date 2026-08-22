package com.kubemanager.ai_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
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
