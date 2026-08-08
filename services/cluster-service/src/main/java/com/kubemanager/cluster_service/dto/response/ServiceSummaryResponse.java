package com.kubemanager.cluster_service.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceSummaryResponse {


    private String name;

    private String namespace;

    private String type;

    private String clusterIP;

    private Integer port;

}
