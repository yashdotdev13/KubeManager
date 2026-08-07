package com.kubemanager.cluster_service.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PodSummaryResponse {


    private String name;

    private String namespace;

    private String status;

    private String node;

    private String podIp;

    private Integer restartCount;
}
