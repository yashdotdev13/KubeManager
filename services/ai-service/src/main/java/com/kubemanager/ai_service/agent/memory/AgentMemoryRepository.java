package com.kubemanager.ai_service.agent.memory;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgentMemoryRepository extends JpaRepository<AgentMemory, UUID> {

    List<AgentMemory>  findByUserIdOrderByUpdatedAtDesc(
            String userId
    );

    List<AgentMemory> findByUserIdAndMemoryTypeOrderByUpdatedAtDesc(
            String userId,
            String memoryType
    );

    List<AgentMemory> findByUserIdOrderByUpdatedAtDesc(
            String userId,
            Pageable pageable
    );
}
