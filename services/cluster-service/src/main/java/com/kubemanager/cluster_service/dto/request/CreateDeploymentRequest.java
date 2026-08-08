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
@AllArgsConstructor
@NoArgsConstructor
public class CreateDeploymentRequest {

    @NotBlank(message = "Deployment name is required.")
    private String name;

    @NotBlank(message = "Namespace is required.")
    private String namespace;

    @NotBlank(message = "Container image is required.")
    private String image;

    @NotNull(message = "Replicas are required.")
    @Min(value = 1, message = "Replicas must be at least 1.")
    private Integer replicas;

    @NotNull(message = "Container port is required.")
    @Min(value = 1)
    private Integer containerPort;
}
