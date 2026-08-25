package com.kubemanager.ai_service.rag;

import com.kubemanager.ai_service.agent.AiTool;

import com.kubemanager.ai_service.agent.tool.ToolDefinition;
import com.kubemanager.ai_service.agent.tool.ToolRegistry;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ToolRegistryIntegrationTest {

    @Autowired
    private ToolRegistry toolRegistry;

    @Test
    void shouldRegisterRagKnowledgeTool() {

        AiTool tool =
                toolRegistry.getTool(
                        "rag_knowledge"
                );

        assertNotNull(
                tool,
                "rag_knowledge tool must be registered."
        );

        assertTrue(
                tool.getName()
                        .equals("rag_knowledge"),
                "Registered tool name must be rag_knowledge."
        );

        System.out.println(
                "\n========== REGISTERED AI TOOLS =========="
        );

        List<ToolDefinition> definitions =
                toolRegistry.getDefinitions();

        definitions.forEach(definition ->
                System.out.println(
                        definition.getName()
                )
        );

        System.out.println(
                "=========================================\n"
        );
    }
}
