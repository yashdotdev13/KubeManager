package com.kubemanager.ai_service.agent.decision;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kubemanager.ai_service.agent.model.AgentRequest;
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
public class AgentDecisionServiceImpl implements AgentDecisionService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final ToolRegistry toolRegistry;

    @Override
    public AgentDecision decide(AgentRequest request) {

        List<ToolDefinition> toolDefinitions =
                toolRegistry.getDefinitions();

        String availableTools =
                buildToolDefinitions(toolDefinitions);

        String prompt = """
                You are the decision-making engine of KubeManager AI.

                Your job is to analyze the user's request and decide
                whether the request:

                1. Can be answered directly using normal conversation.
                2. Requires execution of one of the available tools.

                AVAILABLE TOOLS:

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
                - If the request is normal conversation, return CHAT.
                - The toolName must exactly match the available tool name.
                - The arguments must follow the tool's input schema.

                USER REQUEST:

                %s
                """.formatted(
                availableTools,
                request.getMessage()
        );

        try {

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

    private String buildToolDefinitions(
            List<ToolDefinition> toolDefinitions
    ) {

        if (toolDefinitions == null || toolDefinitions.isEmpty()) {

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

            // Verify that the AI selected an actually registered tool.
            toolRegistry.getTool(decision.getToolName());

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