package com.kubemanager.cluster_service.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateNamespaceRequest {

    @NotBlank(message = "Namespace is required.")
    private String name;
}
