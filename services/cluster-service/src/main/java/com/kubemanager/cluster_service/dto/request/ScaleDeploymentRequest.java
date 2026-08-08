package com.kubemanager.cluster_service.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScaleDeploymentRequest {


    @NotNull(message = "Replicas cannot be null.")
    @Min(value = 0, message = "Replicas cannot be negative.")
    private Integer replicas;
}
