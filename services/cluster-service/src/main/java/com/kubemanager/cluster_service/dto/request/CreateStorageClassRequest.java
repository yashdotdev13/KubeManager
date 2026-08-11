package com.kubemanager.cluster_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStorageClassRequest {

    @NotBlank(message = "StorageClass name is required.")
    private String name;

    @NotBlank(message = "Provisioner is required.")
    private String provisioner;

    private String reclaimPolicy;

    private String volumeBindingMode;

    private Boolean allowVolumeExpansion;

    private Map<String, String> parameters;
}