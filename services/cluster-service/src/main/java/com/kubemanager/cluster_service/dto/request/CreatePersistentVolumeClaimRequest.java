package com.kubemanager.cluster_service.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePersistentVolumeClaimRequest {

    @NotBlank(message = "PVC name is required.")
    private String name;

    @NotBlank(message = "Namespace is required.")
    private String namespace;

    @NotNull(message = "Storage size is required.")
    @Min(value = 1, message = "Storage size must be greater than 0.")
    private Integer storageSize;

    @NotBlank(message = "Storage class is required.")
    private String storageClassName;
}