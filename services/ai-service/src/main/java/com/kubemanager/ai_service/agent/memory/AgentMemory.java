package com.kubemanager.ai_service.agent.memory;


import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "agent_memories",
        indexes = {
                @Index(
                        name = "idx_agent_memory_user_id",
                        columnList = "user_id"
                )
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgentMemory {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "user_id",
            nullable = false,
            length = 100
    )
    private String userId;

    @Column(
            name = "memory_type",
            nullable = false,
            length = 50
    )
    private String memoryType;

    @Column(
            name = "content",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String content;
    @Column(
            name = "source",
            length = 100
    )
    private String source;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        OffsetDateTime now = OffsetDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = OffsetDateTime.now();
    }
}
