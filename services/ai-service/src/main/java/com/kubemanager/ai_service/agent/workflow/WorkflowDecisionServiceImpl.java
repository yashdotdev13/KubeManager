package com.kubemanager.ai_service.agent.workflow;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kubemanager.ai_service.agent.tool.ToolDefinition;
import com.kubemanager.ai_service.agent.tool.ToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowDecisionServiceImpl
        implements WorkflowDecisionService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final ToolRegistry toolRegistry;

    @Override
    public WorkflowDecision decide(
            AgentWorkflowContext workflowContext
    ) {

        if (workflowContext == null) {

            throw new IllegalArgumentException(
                    "Workflow context cannot be null."
            );
        }

        String availableTools =
                buildToolDefinitions(
                        toolRegistry.getDefinitions()
                );

        String executionContext =
                buildExecutionContext(
                        workflowContext
                );

        String prompt = """
                You are the workflow decision engine of
                KubeManager AI.

                You are responsible for deciding the NEXT action
                in an agentic Kubernetes workflow.

                The workflow may contain multiple tool executions.

                After inspecting the current workflow state, you must
                decide whether:

                1. Another Kubernetes tool must be executed.
                2. The workflow has enough information and can complete.

                =====================================================
                AVAILABLE TOOLS
                =====================================================

                %s

                =====================================================
                CURRENT WORKFLOW
                =====================================================

                %s

                =====================================================
                DECISION RULES
                =====================================================

                - Return ONLY valid JSON.
                - Do NOT return markdown.
                - Do NOT wrap JSON in ```json blocks.
                - Do NOT add explanations outside the JSON.
                - Only select tools from AVAILABLE TOOLS.
                - Never invent a tool name.
                - Never invent tool arguments.
                - Never invent cluster IDs.
                - Never invent namespaces.
                - Never invent resource names.
                - Use previous tool results when deciding the
                  next required operation.
                - Do not repeat a tool unnecessarily.
                - If sufficient information exists to answer the
                  user, return COMPLETE.
                - If more information is required, return
                  EXECUTE_TOOL.
                - The selected tool must have all required arguments.
                - If a required argument cannot be resolved safely,
                  return COMPLETE with a response asking the user
                  for the missing information.
                - The current user request is the highest priority.
                - Do not assume that a Kubernetes resource still
                  exists merely because an earlier result mentioned it.

                =====================================================
                RESPONSE FORMAT
                =====================================================

                When another tool is required:

                {
                  "type": "EXECUTE_TOOL",
                  "toolName": "exact_tool_name",
                  "arguments": {
                    "argumentName": "argumentValue"
                  },
                  "response": null,
                  "reasoning": "why this tool is required"
                }

                When the workflow is complete:

                {
                  "type": "COMPLETE",
                  "toolName": null,
                  "arguments": null,
                  "response": "final response to the user",
                  "reasoning": "why no more tools are required"
                }
                """.formatted(
                availableTools,
                executionContext
        );

        try {

            log.debug(
                    "Generating workflow decision. " +
                            "executionCount={}",
                    workflowContext.getExecutionCount()
            );

            String rawResponse =
                    chatClient
                            .prompt()
                            .user(prompt)
                            .call()
                            .content();

            if (rawResponse == null
                    || rawResponse.isBlank()) {

                throw new IllegalStateException(
                        "Workflow decision engine returned " +
                                "an empty response."
                );
            }

            String jsonResponse =
                    cleanJsonResponse(rawResponse);

            log.debug(
                    "Workflow decision response: {}",
                    jsonResponse
            );

            WorkflowDecision decision =
                    objectMapper.readValue(
                            jsonResponse,
                            WorkflowDecision.class
                    );

            validateDecision(decision);

            return decision;

        } catch (Exception exception) {

            log.error(
                    "Failed to generate workflow decision.",
                    exception
            );

            throw new IllegalStateException(
                    "Failed to generate workflow decision.",
                    exception
            );
        }
    }

    private String buildToolDefinitions(
            List<ToolDefinition> toolDefinitions
    ) {

        if (toolDefinitions == null
                || toolDefinitions.isEmpty()) {

            return "No tools are currently available.";
        }

        return toolDefinitions
                .stream()
                .map(tool -> """
                        TOOL NAME:
                        %s

                        DESCRIPTION:
                        %s

                        INPUT SCHEMA:
                        %s
                        """.formatted(
                        tool.getName(),
                        tool.getDescription(),
                        tool.getInputSchema()
                ))
                .collect(Collectors.joining("\n"));
    }

    private String buildExecutionContext(
            AgentWorkflowContext context
    ) {

        StringBuilder builder =
                new StringBuilder();

        builder.append(
                "USER REQUEST:\n"
        );

        builder.append(
                context.getUserMessage()
        );

        builder.append(
                "\n\nEXECUTED TOOL STEPS:\n"
        );

        if (context.getExecutionSteps().isEmpty()) {

            builder.append(
                    "No tools have been executed yet."
            );

        } else {

            context.getExecutionSteps()
                    .forEach(step -> {

                        builder.append(
                                """
                                STEP %d
                                TOOL:
                                %s

                                ARGUMENTS:
                                %s

                                RESULT:
                                %s

                                """.formatted(
                                        step.getStepNumber(),
                                        step.getToolName(),
                                        step.getArguments(),
                                        step.getResponse()
                                ));
                    });
        }

        builder.append(
                "\nACCUMULATED TOOL CONTEXT:\n"
        );

        if (context.getAccumulatedContext().isEmpty()) {

            builder.append(
                    "No accumulated tool context."
            );

        } else {

            builder.append(
                    context.getAccumulatedContext()
            );
        }

        return builder.toString();
    }

    private void validateDecision(
            WorkflowDecision decision
    ) {

        if (decision == null) {

            throw new IllegalStateException(
                    "Workflow decision cannot be null."
            );
        }

        if (decision.getType() == null) {

            throw new IllegalStateException(
                    "Workflow decision type cannot be null."
            );
        }

        if (decision.getType()
                == WorkflowDecisionType.EXECUTE_TOOL) {

            if (decision.getToolName() == null
                    || decision.getToolName().isBlank()) {

                throw new IllegalStateException(
                        "Tool name is required for EXECUTE_TOOL."
                );
            }

            /*
             * Verify that the LLM selected a registered tool.
             */
            toolRegistry.getTool(
                    decision.getToolName()
            );

            if (decision.getArguments() == null) {

                throw new IllegalStateException(
                        "Tool arguments are required for " +
                                "EXECUTE_TOOL."
                );
            }
        }

        if (decision.getType()
                == WorkflowDecisionType.COMPLETE) {

            if (decision.getResponse() == null
                    || decision.getResponse().isBlank()) {

                log.warn(
                        "Workflow completed without a final response."
                );
            }
        }
    }

    private String cleanJsonResponse(
            String response
    ) {

        String cleaned =
                response.trim();

        if (cleaned.startsWith("```json")) {

            cleaned =
                    cleaned
                            .substring(7)
                            .trim();

        } else if (cleaned.startsWith("```")) {

            cleaned =
                    cleaned
                            .substring(3)
                            .trim();
        }

        if (cleaned.endsWith("```")) {

            cleaned =
                    cleaned
                            .substring(
                                    0,
                                    cleaned.length() - 3
                            )
                            .trim();
        }

        return cleaned;
    }
}