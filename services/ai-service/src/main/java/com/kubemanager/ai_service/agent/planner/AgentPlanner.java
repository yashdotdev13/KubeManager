package com.kubemanager.ai_service.agent.planner;

import com.kubemanager.ai_service.agent.model.AgentPlan;
import com.kubemanager.ai_service.agent.model.AgentRequest;

public interface AgentPlanner {


    AgentPlan createPlan(AgentRequest request);
}
