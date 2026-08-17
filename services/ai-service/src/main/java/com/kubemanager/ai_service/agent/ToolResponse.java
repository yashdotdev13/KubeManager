package com.kubemanager.ai_service.agent;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ToolResponse {

    private boolean success;

    private Object data;

    private String message;
}