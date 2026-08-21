package com.kubemanager.ai_service.agent.workflow;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDecision {

    private WorkflowDecisionType type;

    private String toolName;

    private Map<String, Object> arguments;

    private String response;

    private String reasoning;
}