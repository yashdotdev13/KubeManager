package com.kubemanager.ai_service.agent.decision;

import com.kubemanager.ai_service.agent.model.AgentRequest;

public interface AgentDecisionService {


    AgentDecision decide(AgentRequest request);
}
