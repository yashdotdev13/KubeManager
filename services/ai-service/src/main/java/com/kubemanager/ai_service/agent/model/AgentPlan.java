package com.kubemanager.ai_service.agent.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class AgentPlan {

    private boolean requiresTool;

    private String toolName;

    private Map<String, Object> arguments;

    private String reasoning;
}
