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
public class AgentDecisionServiceImpl implements AgentDecisionService {

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

        /*
         * Get the currently authenticated user.
         *
         * AgentContext is stored per user, so the userId is used
         * to retrieve the context of the current conversation.
         */
        UserContext userContext =
                UserContextHolder.getRequiredContext();

        String userId =
                String.valueOf(userContext.getUserId());

        /*
         * Retrieve the previous agent context.
         *
         * This can be null when the user is making their
         * first request.
         */
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

                Your job is to analyze the user's request and decide
                whether the request:

                1. Can be answered directly using normal conversation.
                2. Requires execution of one of the available tools.

                You are operating as an agentic Kubernetes assistant.

                You have access to previous agent context from the
                current user's earlier interaction.

                Use that context when the current request refers to
                something from the previous interaction.

                Examples:

                - "scale it to 5" may refer to the deployment mentioned
                  in the previous interaction.
                - "delete that pod" may refer to the pod identified
                  previously.
                - "show me its details" may refer to the Kubernetes
                  resource from the previous interaction.
                - "restart it" may refer to the deployment previously
                  discussed.

                Do NOT guess when the previous context does not contain
                enough information to resolve the user's request.

                AVAILABLE TOOLS:

                %s

                PREVIOUS AGENT CONTEXT:

                %s

                RESPONSE FORMAT:

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

                RULES:

                - Return ONLY valid JSON.
                - Do NOT return markdown.
                - Do NOT wrap the JSON inside ```json or ``` blocks.
                - Do NOT add explanations outside the JSON.
                - Only use tools that are listed in AVAILABLE TOOLS.
                - Do NOT invent tool names.
                - Do NOT invent tool arguments.
                - Do NOT invent resource IDs such as clusterId.
                - Use a tool only when the user's request actually requires it.
                - If required information for a tool is missing, return CHAT
                  and ask the user for that information.
                - Use previous agent context when it provides information
                  required to understand the current request.
                - If a reference such as "it", "that", "this", "same",
                  or "the deployment" can be resolved from previous
                  context, use the corresponding information.
                - Do NOT invent missing values even when previous context
                  exists.
                - The toolName must exactly match the available tool name.
                - The arguments must follow the tool's input schema.
                - If the current request conflicts with previous context,
                  prioritize the current user request.
                - If the request is normal conversation, return CHAT.

                CURRENT USER REQUEST:

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

            String rawResponse = chatClient
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (rawResponse == null || rawResponse.isBlank()) {

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
     * Builds the tool definitions that are provided to the LLM.
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
     * Converts the existing AgentContext into information
     * that can be supplied to the LLM.
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
             * Verify that the AI selected an actually
             * registered tool.
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

    private String cleanJsonResponse(
            String response
    ) {

        String cleaned = response.trim();

        if (cleaned.startsWith("```json")) {

            cleaned = cleaned
                    .substring(7)
                    .trim();

        } else if (cleaned.startsWith("```")) {

            cleaned = cleaned
                    .substring(3)
                    .trim();
        }

        if (cleaned.endsWith("```")) {

            cleaned = cleaned
                    .substring(
                            0,
                            cleaned.length() - 3
                    )
                    .trim();
        }

        return cleaned;
    }
}