package com.kubemanager.cluster_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageClassSummaryResponse {

    private String name;

    private String provisioner;

    private String reclaimPolicy;

    private String volumeBindingMode;

    private Boolean allowVolumeExpansion;
}