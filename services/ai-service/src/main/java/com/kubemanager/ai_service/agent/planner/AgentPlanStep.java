package com.kubemanager.ai_service.agent.planner;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class AgentPlanStep {

    private int step;

    private PlanStepType type;

    private String toolName;

    private Map<String, Object> arguments;

    private String description;
}