package com.kubemanager.cluster_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersistentVolumeResponse {

    private String name;

    private String status;

    private String capacity;

    private String storageClassName;

    private String accessMode;

    private String reclaimPolicy;

    private String claim;

    private String hostPath;

    private OffsetDateTime creationTimestamp;
}