package com.kubemanager.ai_service.agent.decision;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kubemanager.ai_service.agent.context.AgentContext;
import com.kubemanager.ai_service.agent.context.AgentContextService;
import com.kubemanager.ai_service.agent.model.AgentRequest;
import com.kubemanager.ai_service.agent.tool.ToolDefinition;
import com.kubemanager.ai_service.agent.tool.ToolRegistry;
import com.kubemanager.ai_service.auth.UserContext;
import com.kubemanager.ai_service.auth.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public AgentDecision decide(AgentRequest request) {

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
                String.valueOf(userContext.getUserId());

        AgentContext previousContext =
                agentContextService.getContext(userId);

        List<ToolDefinition> toolDefinitions =
                toolRegistry.getDefinitions();

        String availableTools =
                buildToolDefinitions(toolDefinitions);


        String contextInformation =
                buildContextInformation(previousContext);


        String prompt = """
                You are the decision-making engine of KubeManager AI.

                You are an agentic Kubernetes assistant.

                Your responsibility is to analyze the current user
                request and determine whether:

                1. The request can be answered directly using normal
                   conversation.

                2. The request requires execution of one of the
                   available Kubernetes tools.

                You have access to the previous interaction context
                of the current user.

                Use previous context ONLY when it provides reliable
                information needed to understand the current request.

                =====================================================
                AVAILABLE TOOLS
                =====================================================

                %s

                =====================================================
                PREVIOUS AGENT CONTEXT
                =====================================================

                %s

                =====================================================
                CONTEXT RESOLUTION
                =====================================================

                The previous context may contain:

                - The previous user message.
                - The previous tool that was executed.
                - The arguments used by that tool.
                - The result returned by that tool.

                Use this information to resolve references such as:

                - "it"
                - "that"
                - "this"
                - "the deployment"
                - "the pod"
                - "the namespace"
                - "the same deployment"
                - "the first one"
                - "restart it"
                - "scale it"
                - "delete it"
                - "show its details"

                Example:

                Previous request:
                "Show me the deployments in ai-test."

                Previous tool:
                deployment_list_by_namespace

                Current request:
                "Scale the first one to 5."

                If the previous tool result identifies the deployment
                and the required clusterId and namespace are available,
                resolve the reference and generate the appropriate
                TOOL_CALL.

                Another example:

                Previous request:
                "Show me the pod api-server-123."

                Current request:
                "Delete it."

                If the previous context identifies the pod and contains
                the required clusterId, namespace and pod name,
                generate the appropriate pod deletion TOOL_CALL.

                =====================================================
                IMPORTANT CONTEXT RULES
                =====================================================

                - Previous context is supporting information only.
                - Always prioritize the current user request.
                - Never invent missing values.
                - Never fabricate cluster IDs.
                - Never fabricate namespace names.
                - Never fabricate pod names.
                - Never fabricate deployment names.
                - Never fabricate tool arguments.
                - Do not assume that a previous resource still exists.
                - If the previous context does not contain enough
                  information to safely execute a tool, return CHAT
                  and ask the user for the missing information.
                - Do not use a previous tool result if it is unrelated
                  to the current request.
                - Do not blindly repeat the previous tool.
                - Determine the required action from the current request.
                - Only use tools explicitly listed in AVAILABLE TOOLS.

                =====================================================
                RESPONSE FORMAT
                =====================================================

                For a normal conversational request:

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

                =====================================================
                DECISION RULES
                =====================================================

                - Return ONLY valid JSON.
                - Do NOT return markdown.
                - Do NOT wrap JSON inside ```json or ``` blocks.
                - Do NOT add explanations outside the JSON.
                - Only use tools listed in AVAILABLE TOOLS.
                - The toolName must exactly match a registered tool.
                - Arguments must follow the tool's input schema.
                - Do NOT invent tool names.
                - Do NOT invent arguments.
                - Do NOT invent resource identifiers.
                - Use a tool only when the request requires execution.
                - If required information is missing, return CHAT and
                  ask for that information.
                - If the request is normal conversation, return CHAT.
                - If previous context resolves a reference safely,
                  use the resolved information.
                - If previous context cannot safely resolve the
                  reference, ask the user for clarification.
                - If the current request conflicts with previous
                  context, prioritize the current request.

                =====================================================
                CURRENT USER REQUEST
                =====================================================

                %s
                """.formatted(
                availableTools,
                contextInformation,
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
                    cleanJsonResponse(rawResponse);

            log.debug(
                    "AI decision response: {}",
                    jsonResponse
            );

            AgentDecision decision =
                    objectMapper.readValue(
                            jsonResponse,
                            AgentDecision.class
                    );

            validateDecision(decision);

            return decision;

        } catch (Exception exception) {

            log.error(
                    "Failed to generate agent decision for request: {}",
                    request.getMessage(),
                    exception
            );

            throw new IllegalStateException(
                    "Failed to generate agent decision",
                    exception
            );
        }
    }

    /**
     * Builds the list of tools available to the agent.
     */
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

    /**
     * Converts the previous AgentContext into structured
     * information that can be understood by the LLM.
     */
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
                Previous User Message:
                %s

                Previous Tool:
                %s

                Previous Tool Arguments:
                %s

                Previous Tool Result:
                %s
                """.formatted(
                safeValue(context.getLastUserMessage()),
                safeValue(context.getLastToolName()),
                safeValue(context.getLastToolArguments()),
                safeValue(context.getLastToolResult())
        );
    }

    /**
     * Prevents null values from appearing in the prompt.
     */
    private String safeValue(Object value) {

        if (value == null) {
            return "Not available.";
        }

        return String.valueOf(value);
    }

    /**
     * Validates the decision generated by the LLM.
     */
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

        if (decision.getType() == DecisionType.TOOL_CALL) {

            if (decision.getToolName() == null
                    || decision.getToolName().isBlank()) {

                throw new IllegalStateException(
                        "Tool name is required for TOOL_CALL"
                );
            }

            /*
             * Verify that the selected tool actually exists
             * in the ToolRegistry.
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

        if (decision.getType() == DecisionType.CHAT) {

            if (decision.getResponse() == null
                    || decision.getResponse().isBlank()) {

                log.warn(
                        "AI returned CHAT decision without a response"
                );
            }
        }
    }

    /**
     * Removes markdown code fences if the LLM returns JSON
     * wrapped inside them.
     */
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