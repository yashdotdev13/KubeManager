package com.kubemanager.ai_service.agent.memory;

public interface MemoryExtractionService {


    MemoryExtractionResult extract(
            String userMessage,
            String toolName,
            Object toolResult
    );
}
