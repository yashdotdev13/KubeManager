package com.kubemanager.ai_service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentCreateRequest {

    private String name;

    private String namespace;

    private String image;

    private Integer replicas;
}