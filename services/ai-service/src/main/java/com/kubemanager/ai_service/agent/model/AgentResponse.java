package com.kubemanager.ai_service.agent.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AgentResponse {

    private String message;

    private Object data;
}