package com.kubemanager.cluster_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PodExecResponse {

    private String podName;

    private String namespace;

    private String containerName;

    private String command;

    private String output;

    private String error;
}