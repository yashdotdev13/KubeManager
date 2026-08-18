package com.kubemanager.ai_service.agent.model;

import com.kubemanager.ai_service.agent.planner.AgentPlanStep;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class AgentPlan {

    private List<AgentPlanStep> steps;
}