package com.kubemanager.ai_service.dto;

import lombok.Data;

@Data
public class NamespaceInfoServiceApiResponse {

    private boolean success;

    private String message;

    private NamespaceResponse data;
}