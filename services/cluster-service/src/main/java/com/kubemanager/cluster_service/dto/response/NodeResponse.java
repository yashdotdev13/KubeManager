package com.kubemanager.cluster_service.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeResponse {

    private String name;

    private String status;

    private String role;

    private String kubernetesVersion;

    private String operatingSystem;

    private String architecture;

    private String kernelVersion;

    private String containerRuntime;

    private String osImage;

    private String internalIp;

    private String externalIp;

    private Map<String, String> labels;

    private Map<String, String> capacity;

    private Map<String, String> allocatable;
}