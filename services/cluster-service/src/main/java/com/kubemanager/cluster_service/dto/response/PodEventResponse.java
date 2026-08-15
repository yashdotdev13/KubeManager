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
public class PodEventResponse {

    private String type;

    private String reason;

    private String message;

    private String source;

    private Integer count;

    private OffsetDateTime firstTimestamp;

    private OffsetDateTime lastTimestamp;

}
