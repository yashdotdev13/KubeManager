package com.kubemanager.ai_service.agent.execution;

import com.kubemanager.ai_service.agent.model.AgentRequest;

public interface  AgentExecutionService {

    AgentExecutionContext execute(AgentRequest request);
}
