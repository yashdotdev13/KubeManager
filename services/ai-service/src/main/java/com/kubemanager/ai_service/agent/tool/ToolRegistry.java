package com.kubemanager.ai_service.agent.tool;

import com.kubemanager.ai_service.agent.AiTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ToolRegistry {

    private final List<AiTool> aiTools;

    public AiTool getTool(String toolName) {

        return aiTools.stream()
                .filter(tool -> tool.getName().equals(toolName))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unknown AI tool: " + toolName
                        )
                );
    }

    public List<ToolDefinition> getDefinitions() {

        return aiTools.stream()
                .map(AiTool::getToolDefinition)
                .toList();
    }
}