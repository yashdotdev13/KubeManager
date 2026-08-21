package com.kubemanager.ai_service.agent.memory;

import java.util.List;

public interface MemoryRetrievalService {

    List<AgentMemory> retrieveRelevantMemories(
            String userId,
            String userMessage
    );
}