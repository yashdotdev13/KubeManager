package com.kubemanager.ai_service.dto;

import lombok.Data;

@Data
public class PodDeleteServiceApiResponse {

    private boolean success;

    private String message;

    private Object data;
}