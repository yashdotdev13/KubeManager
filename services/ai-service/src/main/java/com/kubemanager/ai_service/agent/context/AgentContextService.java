package com.kubemanager.ai_service.agent.context;

public interface  AgentContextService {

    AgentContext getContext();

    void updateContext(AgentContext context);

    void clearContext();
}
