package com.kubemanager.cluster_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePersistentVolumeRequest {

    @NotBlank(message = "PV name is required.")
    private String name;

    @Min(value = 1, message = "Storage size must be greater than 0.")
    private Integer storageSize;

    @NotBlank(message = "Storage class name is required.")
    private String storageClassName;

    @NotBlank(message = "Host path is required.")
    private String hostPath;
}