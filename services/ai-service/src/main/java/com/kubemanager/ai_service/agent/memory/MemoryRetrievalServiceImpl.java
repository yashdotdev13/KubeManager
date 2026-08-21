package com.kubemanager.ai_service.agent.memory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemoryRetrievalServiceImpl implements MemoryRetrievalService {


    private static final int DEFAULT_MEMORY_LIMIT = 10;

    private final AgentMemoryService agentMemoryService;

    @Override
    public List<AgentMemory> retrieveRelevantMemories(
            String userId,
            String userMessage
    ) {

        if (userId == null || userId.isBlank()) {

            throw new IllegalArgumentException(
                    "User ID cannot be null or blank."
            );
        }

        if (userMessage == null
                || userMessage.isBlank()) {

            return List.of();
        }

        try {

            /*
             * For the first version, retrieve the most recent
             * memories belonging to the current user.
             *
             * Semantic/vector retrieval will be introduced
             * later as part of the RAG layer.
             */
            return agentMemoryService.getMemories(
                    userId,
                    DEFAULT_MEMORY_LIMIT
            );

        } catch (Exception exception) {

            log.error(
                    "Failed to retrieve memories for userId={}",
                    userId,
                    exception
            );

            /*
             * Memory retrieval is auxiliary functionality.
             * Failure must not stop the agent from processing
             * the user's Kubernetes request.
             */
            return List.of();
        }
    }
}