package com.kubemanager.ai_service.agent.reasoning;

public interface AgentReasoningService {

    String generateFinalResponse(
            String userMessage,
            String toolName,
            Object toolResult
    );
}