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
public class PersistentVolumeClaimResponse {

    private String name;

    private String namespace;

    private String status;

    private String storageClassName;

    private String storage;

    private String volumeName;

    private String accessMode;

    private OffsetDateTime creationTimestamp;
}