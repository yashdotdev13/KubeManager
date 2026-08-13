package com.kubemanager.cluster_service.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateStatefulSetRequest {

    @NotBlank(message = "StatefulSet name is required.")
    private String name;

    @NotNull(message = "Replicas are required.")
    @Min(value = 0, message = "Replicas cannot be negative.")
    private Integer replicas;

    @NotBlank(message = "Container name is required.")
    private String containerName;

    @NotBlank(message = "Container image is required.")
    private String image;

    @NotBlank(message = "Service name is required.")
    private String serviceName;

    private Integer containerPort;

    private Map<String, String> labels;

    private Map<String, String> environment;
}