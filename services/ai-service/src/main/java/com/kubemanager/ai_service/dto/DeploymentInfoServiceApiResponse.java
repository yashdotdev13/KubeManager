package com.kubemanager.ai_service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentInfoServiceApiResponse {

    private boolean success;

    private String message;

    private DeploymentInfoResponse data;
}
