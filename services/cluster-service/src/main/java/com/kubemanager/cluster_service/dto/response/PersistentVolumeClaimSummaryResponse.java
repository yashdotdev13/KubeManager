package com.kubemanager.cluster_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersistentVolumeClaimSummaryResponse {

    private String name;

    private String namespace;

    private String status;

    private String storageClassName;

    private String storage;
}