package com.kubemanager.ai_service.agent.workflow;


import com.kubemanager.ai_service.agent.tool.ToolResponse;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class AgentWorkflowContext {

    private final String userMessage;

    private final List<ToolExecutionStep> executionSteps =
            new ArrayList<>();

    private final Map<String, Object> accumulatedContext =
            new LinkedHashMap<>();

    public AgentWorkflowContext(
            String userMessage
    ) {

        if (userMessage == null
                || userMessage.isBlank()) {

            throw new IllegalArgumentException(
                    "User message cannot be null or blank."
            );
        }
        this.userMessage = userMessage;
    }

    public void addExecutionStep(
            ToolExecutionStep step
    ) {

        if (step == null) {
            return;
        }
        executionSteps.add(step);
    }

    public void addToolResult(
            String toolName,
            ToolResponse response
    ) {

        if (toolName == null
                || toolName.isBlank()
                || response == null) {

            return;
        }

        accumulatedContext.put(
                toolName,
                response.getData()
        );
    }

    public int getExecutionCount() {
        return executionSteps.size();
    }

    public boolean hasExecuted(
            String toolName
    ) {

        return executionSteps
                .stream()
                .anyMatch(step ->
                        toolName.equals(
                                step.getToolName()
                        )
                );
    }
}