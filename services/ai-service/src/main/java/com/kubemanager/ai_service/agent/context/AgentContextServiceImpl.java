package com.kubemanager.ai_service.agent.context;

import org.springframework.stereotype.Service;

@Service
public class AgentContextServiceImpl implements AgentContextService {

    private final ThreadLocal<AgentContext> contextHolder =
            new ThreadLocal<>();

    @Override
    public AgentContext getContext() {
        return contextHolder.get();
    }

    @Override
    public void updateContext(AgentContext context) {
        contextHolder.set(context);
    }

    @Override
    public void clearContext() {
        contextHolder.remove();
    }
}