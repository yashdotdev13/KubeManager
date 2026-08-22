package com.kubemanager.ai_service.agent.context;

public interface AgentEvidenceContextService {

    AgentEvidenceContext update(
            AgentEvidenceContext currentContext,
            String toolName,
            Object toolResult
    );
}
