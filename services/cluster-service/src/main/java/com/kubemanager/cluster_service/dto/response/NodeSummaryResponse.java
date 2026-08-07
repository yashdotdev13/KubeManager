package com.kubemanager.cluster_service.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NodeSummaryResponse {

    private String name;

    private String status;

    private String role;

    private String kubernetesVersion;

    private String internalIp;

    private String operatingSystem;

    private String architecture;
}
