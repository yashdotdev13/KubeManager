package com.kubemanager.ai_service.agent.decision;


import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class AgentDecision {

    private DecisionType type;

    private String toolName;

    private Map<String, Object> arguments;

    private String response;
}
