package com.kubemanager.cluster_service.dto.response;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PodDescribeResponse {

    private String name;

    private String namespace;

    private String uid;

    private String nodeName;

    private String podIp;

    private String hostIp;

    private String phase;

    private String startTime;

    private String serviceAccount;

    private List<ContainerInfo> containers;

    private List<ConditionInfo> conditions;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContainerInfo {

        private String name;

        private String image;

        private String imageId;

        private String ready;

        private String restartCount;

        private String state;

        private String reason;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConditionInfo {

        private String type;

        private String status;

        private String reason;

        private String message;

        private String lastTransitionTime;
    }
}