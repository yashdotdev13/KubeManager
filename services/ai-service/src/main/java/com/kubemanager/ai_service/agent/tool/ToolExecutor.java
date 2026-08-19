package com.kubemanager.ai_service.agent.tool;

import com.kubemanager.ai_service.agent.AiTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
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

        if (request.getToolName() == null
                || request.getToolName().isBlank()) {

            throw new IllegalArgumentException(
                    "Tool name cannot be null or blank."
            );
        }

        AiTool tool;

        try {

            tool = toolRegistry.getTool(
                    request.getToolName()
            );

        } catch (IllegalArgumentException exception) {

            log.warn(
                    "Attempted to execute unknown AI tool: {}",
                    request.getToolName()
            );

            throw exception;
        }

        if (request.getArguments() == null) {

            throw new IllegalArgumentException(
                    "Tool arguments cannot be null."
            );
        }

        log.info(
                "Executing AI tool: {}",
                request.getToolName()
        );

        try {

            ToolResponse response =
                    tool.execute(request);

            if (response == null) {

                throw new IllegalStateException(
                        "Tool returned a null response: "
                                + request.getToolName()
                );
            }

            return response;

        } catch (Exception exception) {

            log.error(
                    "Tool execution failed: {}",
                    request.getToolName(),
                    exception
            );

            throw exception;
        }
    }
}