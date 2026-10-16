
package com.kubemanager.ai_service.agent.workflow;

import com.kubemanager.ai_service.agent.tool.ToolResponse;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class AgentWorkflowContext {

    private final String userMessage;

    /**
     * Persistent memories retrieved for the current user.
     *
     * These memories can provide additional context when
     * the AI decides which action to execute next.
     */
    private final List<String> persistentMemories;

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

    /**
     * Backward-compatible constructor for existing callers.
     */
    public AgentWorkflowContext(String userMessage) {
        this(userMessage, Collections.emptyList());
    }

    /**
     * Creates a workflow context with persistent memories.
     *
     * @param userMessage       the current user request
     * @param persistentMemories memories retrieved for the user
     */
    public AgentWorkflowContext(
            String userMessage,
            List<String> persistentMemories
    ) {

        if (userMessage == null || userMessage.isBlank()) {
            throw new IllegalArgumentException(
                    "User message cannot be null or blank."
            );
        }

        this.userMessage = userMessage;

        this.persistentMemories =
                persistentMemories == null
                        ? Collections.emptyList()
                        : List.copyOf(persistentMemories);
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

        if (toolName == null || toolName.isBlank()) {
            return false;
        }

        return executionSteps
                .stream()
                .anyMatch(step ->
                        toolName.equals(step.getToolName())
                );
    }

    public Object getToolResult(
            String toolName
    ) {

        if (toolName == null || toolName.isBlank()) {
            return null;
        }

        return accumulatedContext.get(toolName);
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
