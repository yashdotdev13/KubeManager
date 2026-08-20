package com.kubemanager.ai_service.dto;

import lombok.Data;

@Data
public class NamespaceApiResponse {

    private boolean success;

    private String message;

    private NamespaceResponse data;
}
