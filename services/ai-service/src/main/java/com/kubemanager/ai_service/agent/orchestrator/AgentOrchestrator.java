package com.kubemanager.ai_service.agent.orchestrator;

import com.kubemanager.ai_service.agent.model.AgentRequest;
import com.kubemanager.ai_service.agent.model.AgentResponse;

public interface AgentOrchestrator {

    AgentResponse process(AgentRequest request);
}
