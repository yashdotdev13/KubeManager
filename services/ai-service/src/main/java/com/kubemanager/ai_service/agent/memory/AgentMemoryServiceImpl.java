package com.kubemanager.ai_service.agent.memory;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AgentMemoryServiceImpl
        implements AgentMemoryService {

    private final AgentMemoryRepository agentMemoryRepository;

    @Override
    public AgentMemory saveMemory(
            String userId,
            String memoryType,
            String content,
            String source
    ) {

        validateUserId(userId);
        validateContent(content);

        if (memoryType == null || memoryType.isBlank()) {
            throw new IllegalArgumentException(
                    "Memory type cannot be null or blank."
            );
        }

        AgentMemory memory =
                AgentMemory.builder()
                        .userId(userId)
                        .memoryType(memoryType)
                        .content(content)
                        .source(source)
                        .build();

        return agentMemoryRepository.save(memory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentMemory> getMemories(
            String userId
    ) {

        validateUserId(userId);

        return agentMemoryRepository
                .findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentMemory> getMemories(
            String userId,
            int limit
    ) {

        validateUserId(userId);

        if (limit <= 0) {
            throw new IllegalArgumentException(
                    "Memory limit must be greater than zero."
            );
        }

        return agentMemoryRepository
                .findByUserIdOrderByUpdatedAtDesc(
                        userId,
                        PageRequest.of(0, limit)
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentMemory> getMemoriesByType(
            String userId,
            String memoryType
    ) {

        validateUserId(userId);

        if (memoryType == null || memoryType.isBlank()) {
            throw new IllegalArgumentException(
                    "Memory type cannot be null or blank."
            );
        }

        return agentMemoryRepository
                .findByUserIdAndMemoryTypeOrderByUpdatedAtDesc(
                        userId,
                        memoryType
                );
    }

    @Override
    public void deleteMemory(
            String userId,
            UUID memoryId
    ) {

        validateUserId(userId);

        if (memoryId == null) {
            throw new IllegalArgumentException(
                    "Memory ID cannot be null."
            );
        }

        AgentMemory memory =
                agentMemoryRepository.findById(memoryId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Memory not found."
                                )
                        );

        /*
         * Never allow one user to delete another user's memory.
         */
        if (!userId.equals(memory.getUserId())) {
            throw new IllegalArgumentException(
                    "Memory does not belong to the current user."
            );
        }

        agentMemoryRepository.delete(memory);
    }

    @Override
    public void clearMemories(
            String userId
    ) {

        validateUserId(userId);

        List<AgentMemory> memories =
                agentMemoryRepository
                        .findByUserIdOrderByUpdatedAtDesc(userId);

        if (!memories.isEmpty()) {
            agentMemoryRepository.deleteAll(memories);
        }
    }

    private void validateUserId(
            String userId
    ) {

        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException(
                    "User ID cannot be null or blank."
            );
        }
    }

    private void validateContent(
            String content
    ) {

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "Memory content cannot be null or blank."
            );
        }
    }
}