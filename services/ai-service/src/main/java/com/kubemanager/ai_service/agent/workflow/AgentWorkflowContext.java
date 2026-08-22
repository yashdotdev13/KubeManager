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

    /**
     * Accumulated evidence collected during the current workflow.
     *
     * Example:
     *
     * deployment_info -> deployment data
     * pod_info        -> pod data
     * pod_logs        -> logs
     * kubernetes_events -> events
     * kubernetes_metrics -> metrics
     */
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

        /*
         * Do not add failed tool executions to the
         * Kubernetes evidence context.
         */
        if (!response.isSuccess()) {

            return;
        }

        /*
         * Ignore empty tool results.
         */
        if (response.getData() == null) {

            return;
        }

        /*
         * Store the tool result using the tool name
         * as the evidence category.
         *
         * This allows the workflow to accumulate
         * evidence across multiple tool executions.
         */
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

        if (toolName == null
                || toolName.isBlank()) {

            return false;
        }

        return executionSteps
                .stream()
                .anyMatch(step ->
                        toolName.equals(
                                step.getToolName()
                        )
                );
    }

    public Object getToolResult(
            String toolName
    ) {

        if (toolName == null
                || toolName.isBlank()) {

            return null;
        }

        return accumulatedContext.get(
                toolName
        );
    }

    public boolean hasToolResult(
            String toolName
    ) {

        return getToolResult(toolName) != null;
    }

    public void clearAccumulatedContext() {

        accumulatedContext.clear();
    }
}