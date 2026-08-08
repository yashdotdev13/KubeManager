package com.kubemanager.cluster_service.dto.request;


import com.kubemanager.cluster_service.enums.ServiceType;
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
public class CreateServiceRequest {

    @NotBlank(message = "Service name is required.")
    private String name;

    @NotBlank(message = "Namespace is required.")
    private String namespace;

    @NotBlank(message = "Selector is required.")
    private String selector;

    @NotNull(message = "Port is required.")
    @Min(value = 1)
    private Integer port;

    @NotNull(message = "Target port is required.")
    @Min(value = 1)
    private Integer targetPort;

    @NotNull(message = "Service type is required.")
    private ServiceType type;
}
