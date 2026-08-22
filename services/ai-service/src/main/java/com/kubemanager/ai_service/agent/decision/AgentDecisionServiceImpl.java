package com.kubemanager.ai_service.agent.decision;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kubemanager.ai_service.agent.context.AgentContext;
import com.kubemanager.ai_service.agent.context.AgentContextService;
import com.kubemanager.ai_service.agent.model.AgentRequest;
import com.kubemanager.ai_service.agent.tool.ToolDefinition;
import com.kubemanager.ai_service.agent.tool.ToolRegistry;
import com.kubemanager.ai_service.agent.workflow.AgentWorkflowContext;
import com.kubemanager.ai_service.auth.UserContext;
import com.kubemanager.ai_service.auth.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentDecisionServiceImpl
        implements AgentDecisionService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final ToolRegistry toolRegistry;
    private final AgentContextService agentContextService;

    @Override
    public AgentDecision decide(
            AgentRequest request,
            AgentWorkflowContext workflowContext
    ) {

        if (request == null
                || request.getMessage() == null
                || request.getMessage().isBlank()) {

            throw new IllegalArgumentException(
                    "Agent request cannot be null or blank."
            );
        }
        UserContext userContext =
                UserContextHolder.getRequiredContext();

        String userId =
                String.valueOf(
                        userContext.getUserId()
                );
        AgentContext previousContext =
                agentContextService.getContext(
                        userId
                );
        List<ToolDefinition> toolDefinitions =
                toolRegistry.getDefinitions();

        String availableTools =
                buildToolDefinitions(
                        toolDefinitions
                );
        String contextInformation =
                buildContextInformation(
                        previousContext
                );
        String workflowEvidence =
                buildWorkflowEvidence(
                        workflowContext
                );

        String prompt = """
                You are the decision-making engine of KubeManager AI.

                You are an agentic Kubernetes assistant.

                Your responsibility is to analyze the user's request
                and determine the next appropriate action.

                A request may:

                1. Be answered directly using normal conversation.
                2. Require execution of a Kubernetes tool.
                3. Require another tool based on evidence already
                   collected during the current workflow.

                ----------------------------------------------------
                AVAILABLE TOOLS
                ----------------------------------------------------

                %s

                ----------------------------------------------------
                PREVIOUS AGENT CONTEXT
                ----------------------------------------------------

                %s

                ----------------------------------------------------
                CURRENT WORKFLOW EVIDENCE
                ----------------------------------------------------

                %s

                The CURRENT WORKFLOW EVIDENCE represents information
                already collected from Kubernetes tools during the
                current investigation.

                Treat this evidence as authoritative information
                returned by the Kubernetes environment.

                Use it to determine what information is already
                available and what additional information is required.

                Do NOT unnecessarily execute a tool when the required
                information is already available in the evidence.

                If additional Kubernetes information is required,
                select the appropriate available tool.

                ----------------------------------------------------
                IMPORTANT AGENTIC BEHAVIOR
                ----------------------------------------------------

                When investigating a Kubernetes problem, reason
                progressively.

                Example investigation:

                deployment_info
                    ↓
                pod_info
                    ↓
                pod_logs
                    ↓
                kubernetes_events
                    ↓
                kubernetes_metrics

                Do not assume that every investigation requires
                every tool.

                Select the next tool based on the evidence already
                collected and the user's request.

                If the evidence already contains enough information
                to answer the user's request, do not call another
                unnecessary tool.

                If a tool result reveals a problem that requires
                additional Kubernetes information, use another
                appropriate tool.

                Do NOT invent Kubernetes information.

                Do NOT invent cluster IDs.

                Do NOT invent namespace names.

                Do NOT invent pod names.

                Do NOT invent deployment names.

                Resolve references such as:

                "it"
                "that pod"
                "that deployment"
                "the same pod"

                using previous context or current workflow evidence
                when possible.

                If the reference cannot be resolved safely,
                ask the user for the missing information.

                ----------------------------------------------------
                RESPONSE FORMAT
                ----------------------------------------------------

                For normal conversation:

                {
                  "type": "CHAT",
                  "toolName": null,
                  "arguments": null,
                  "response": "your response"
                }

                For a tool request:

                {
                  "type": "TOOL_CALL",
                  "toolName": "exact_tool_name",
                  "arguments": {
                    "argumentName": "argumentValue"
                  },
                  "response": null
                }

                ----------------------------------------------------
                RULES
                ----------------------------------------------------

                - Return ONLY valid JSON.
                - Do NOT return markdown.
                - Do NOT wrap JSON inside code blocks.
                - Do NOT add explanations outside JSON.
                - Only use tools listed in AVAILABLE TOOLS.
                - Never invent tool names.
                - Never invent tool arguments.
                - Never invent Kubernetes resource IDs.
                - Tool name must exactly match the registered tool.
                - Arguments must follow the tool input schema.
                - Use previous agent context when relevant.
                - Use current workflow evidence when relevant.
                - Prefer current user instructions over previous context.
                - If required information is missing, return CHAT and
                  ask the user for it.
                - If no tool is required, return CHAT.

                ----------------------------------------------------
                CURRENT USER REQUEST
                ----------------------------------------------------

                %s
                """.formatted(
                availableTools,
                contextInformation,
                workflowEvidence,
                request.getMessage()
        );

        try {

            log.debug(
                    "Generating agent decision for userId={}",
                    userId
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
                        "AI decision engine returned an empty response"
                );
            }

            String jsonResponse =
                    cleanJsonResponse(
                            rawResponse
                    );

            log.debug(
                    "AI decision response: {}",
                    jsonResponse
            );

            AgentDecision decision =
                    objectMapper.readValue(
                            jsonResponse,
                            AgentDecision.class
                    );

            validateDecision(
                    decision
            );

            return decision;

        } catch (Exception exception) {

            log.error(
                    "Failed to generate agent decision " +
                            "for request: {}",
                    request.getMessage(),
                    exception
            );

            throw new IllegalStateException(
                    "Failed to generate agent decision",
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
                .collect(
                        Collectors.joining("\n")
                );
    }

    private String buildContextInformation(
            AgentContext context
    ) {

        if (context == null) {

            return """
                    No previous agent context is available.

                    This is either the user's first request or
                    no context has been stored yet.
                    """;
        }

        return """
                User ID:
                %s

                Last User Message:
                %s

                Last Tool Name:
                %s

                Last Tool Arguments:
                %s

                Last Tool Result:
                %s
                """.formatted(
                context.getUserId(),
                context.getLastUserMessage(),
                context.getLastToolName(),
                context.getLastToolArguments(),
                context.getLastToolResult()
        );
    }

    private String buildWorkflowEvidence(
            AgentWorkflowContext workflowContext
    ) {

        if (workflowContext == null) {

            return """
                    No current workflow evidence is available.
                    """;
        }

        Map<String, Object> evidence =
                workflowContext
                        .getAccumulatedContext();

        if (evidence == null
                || evidence.isEmpty()) {

            return """
                    No Kubernetes evidence has been collected
                    during the current workflow.
                    """;
        }

        return evidence
                .entrySet()
                .stream()
                .map(entry -> """

                        TOOL:
                        %s

                        RESULT:
                        %s
                        """.formatted(
                        entry.getKey(),
                        entry.getValue()
                ))
                .collect(
                        Collectors.joining("\n")
                );
    }

    private void validateDecision(
            AgentDecision decision
    ) {

        if (decision == null) {

            throw new IllegalStateException(
                    "AI decision cannot be null"
            );
        }

        if (decision.getType() == null) {

            throw new IllegalStateException(
                    "AI decision type cannot be null"
            );
        }

        if (decision.getType()
                == DecisionType.TOOL_CALL) {

            if (decision.getToolName() == null
                    || decision.getToolName().isBlank()) {

                throw new IllegalStateException(
                        "Tool name is required for TOOL_CALL"
                );
            }

            /*
             * Verify that the selected tool actually
             * exists in the registry.
             */
            toolRegistry.getTool(
                    decision.getToolName()
            );

            if (decision.getArguments() == null) {

                throw new IllegalStateException(
                        "Tool arguments are required for TOOL_CALL"
                );
            }
        }

        if (decision.getType()
                == DecisionType.CHAT) {

            if (decision.getResponse() == null
                    || decision.getResponse().isBlank()) {

                log.warn(
                        "AI returned CHAT decision " +
                                "without a response"
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