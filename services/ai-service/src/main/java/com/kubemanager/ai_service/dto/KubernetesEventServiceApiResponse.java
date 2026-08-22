package com.kubemanager.ai_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KubernetesEventServiceApiResponse {

    private boolean success;

    private String message;

    private List<KubernetesEventResponse> data;
}