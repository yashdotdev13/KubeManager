package com.kubemanager.ai_service.agent.context;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AgentContextServiceImpl implements AgentContextService {

    private final Map<String, AgentContext> contexts =
            new ConcurrentHashMap<>();

    @Override
    public AgentContext getContext(String userId) {

        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException(
                    "User ID cannot be null or blank."
            );
        }

        return contexts.get(userId);
    }

    @Override
    public void updateContext(
            String userId,
            AgentContext context
    ) {

        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException(
                    "User ID cannot be null or blank."
            );
        }

        if (context == null) {
            throw new IllegalArgumentException(
                    "Agent context cannot be null."
            );
        }

        contexts.put(userId, context);
    }

    @Override
    public void clearContext(String userId) {

        if (userId == null || userId.isBlank()) {
            return;
        }

        contexts.remove(userId);
    }
}