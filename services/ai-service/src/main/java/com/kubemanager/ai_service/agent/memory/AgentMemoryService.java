package com.kubemanager.ai_service.agent.memory;

import java.util.List;
import java.util.UUID;

public interface  AgentMemoryService {

    AgentMemory saveMemory(String userId, String memoryType,
                           String content,
                           String source);


    List<AgentMemory> getMemories(String userId);

    List<AgentMemory> getMemories(String userId, int limit);


    List<AgentMemory> getMemoriesByType(String userId, String memoryType);

    void deleteMemory(String userId, UUID memoryId);

    void clearMemories(String userId);
}
