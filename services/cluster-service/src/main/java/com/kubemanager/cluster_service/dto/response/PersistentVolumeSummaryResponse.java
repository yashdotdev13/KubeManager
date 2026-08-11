package com.kubemanager.cluster_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersistentVolumeSummaryResponse {

    private String name;

    private String status;

    private String capacity;

    private String storageClassName;

    private String accessMode;

    private String claim;
}