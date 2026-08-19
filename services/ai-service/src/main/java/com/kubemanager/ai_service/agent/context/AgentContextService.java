package com.kubemanager.ai_service.agent.context;


public interface AgentContextService {

    AgentContext getContext(String userId);

    void updateContext(String userId, AgentContext context);

    void clearContext(String userId);
}