package com.kubemanager.cluster_service.dto.request;

import com.kubemanager.cluster_service.enums.ClusterEnvironment;
import com.kubemanager.cluster_service.enums.ClusterProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClusterRequest {

    @NotBlank(message = "Cluster name is required.")
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull(message = "Cluster provider is required.")
    private ClusterProvider provider;

    @NotNull(message = "Cluster environment is required.")
    private ClusterEnvironment environment;
}