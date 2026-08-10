package com.kubemanager.cluster_service.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateIngressRequest {

    @NotBlank(message = "Ingress name is required.")
    private String name;

    @NotBlank(message = "Namespace is required.")
    private String namespace;

    @NotBlank(message = "Host is required.")
    private String host;

    @NotBlank(message = "Path is required.")
    private String path;

    @NotBlank(message = "Service name is required.")
    private String serviceName;

    @NotNull(message = "Service port is required.")
    @Min(1)
    private Integer servicePort;
}