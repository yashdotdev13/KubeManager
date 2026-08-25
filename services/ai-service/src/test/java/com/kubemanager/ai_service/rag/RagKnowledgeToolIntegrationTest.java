package com.kubemanager.ai_service.rag;


import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;

import com.kubemanager.ai_service.agent.tool.rag.RagKnowledgeTool;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class RagKnowledgeToolIntegrationTest {

    @Autowired
    private RagKnowledgeTool ragKnowledgeTool;

    @Test
    void shouldRetrieveKubernetesKnowledge() {

        ToolRequest request =
                ToolRequest.builder()
                        .arguments(
                                Map.of(
                                        "query",
                                        "What commonly causes CrashLoopBackOff?"
                                )
                        )
                        .build();

        ToolResponse response =
                ragKnowledgeTool.execute(
                        request
                );

        assertNotNull(
                response,
                "Tool response must not be null."
        );

        assertTrue(
                response.isSuccess(),
                "RAG knowledge tool should execute successfully."
        );

        assertNotNull(
                response.getData(),
                "RAG tool response data must not be null."
        );

        System.out.println(
                "\n========== RAG KNOWLEDGE TOOL =========="
        );

        System.out.println(
                "SUCCESS: "
                        + response.isSuccess()
        );

        System.out.println(
                "MESSAGE: "
                        + response.getMessage()
        );

        System.out.println(
                "DATA:"
        );

        System.out.println(
                response.getData()
        );

        System.out.println(
                "========================================\n"
        );

        assertFalse(
                response.getData().toString().isBlank(),
                "RAG tool should return knowledge."
        );
    }
}