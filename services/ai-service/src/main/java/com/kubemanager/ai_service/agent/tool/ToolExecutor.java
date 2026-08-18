package com.kubemanager.ai_service.agent.tool;

import com.kubemanager.ai_service.agent.AiTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ToolExecutor {

    private final ToolRegistry toolRegistry;

    public ToolResponse execute(ToolRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Tool request cannot be null."
            );
        }

        if (request.getToolName() == null ||
                request.getToolName().isBlank()) {

            throw new IllegalArgumentException(
                    "Tool name cannot be null or blank."
            );
        }

        AiTool tool = toolRegistry.getTool(
                request.getToolName()
        );

        return tool.execute(request);
    }
}