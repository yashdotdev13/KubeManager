package com.kubemanager.cluster_service.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NodeDrainResponse {

    private String nodeName;

    private String status;

    private boolean cordoned;

    private List<String> evictedPods;

    private List<String> skippedPods;

    private List<String> failedPods;

    private String message;
}