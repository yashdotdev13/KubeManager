package com.kubemanager.ai_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterServiceApiResponse {

    private boolean success;

    private String message;

    private ClusterHealthResponse data;

    private LocalDateTime timestamp;

    private String traceId;
}
