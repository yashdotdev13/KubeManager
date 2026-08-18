package com.kubemanager.ai_service.agent.decision;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kubemanager.ai_service.agent.model.AgentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentDecisionServiceImpl implements AgentDecisionService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Override
    public AgentDecision decide(AgentRequest request) {

        String prompt = """
                You are the decision-making engine of KubeManager AI.

                Your job is to decide whether the user's request:

                1. Can be answered directly using normal conversation.
                2. Requires execution of an available tool.

                AVAILABLE TOOL:

                TOOL NAME:
                cluster_health

                DESCRIPTION:
                Checks the health status of a Kubernetes cluster.

                TOOL INPUT:

                clusterId:
                UUID of the Kubernetes cluster.

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
                  "toolName": "cluster_health",
                  "arguments": {
                    "clusterId": "UUID"
                  },
                  "response": null
                }

                RULES:

                - Return ONLY valid JSON.
                - Do NOT return markdown.
                - Do NOT wrap the JSON inside ```json or ``` blocks.
                - Do NOT add explanations outside the JSON.
                - Do NOT invent a clusterId.
                - If the user requests cluster health and provides a valid clusterId,
                  return TOOL_CALL.
                - If the user requests cluster health but does not provide a clusterId,
                  return CHAT and ask the user for the clusterId.
                - If the request is normal conversation, return CHAT.
                - Only use the available tool when required.
                - Do not invent tool names.

                USER REQUEST:

                %s
                """.formatted(request.getMessage());

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

            String jsonResponse = cleanJsonResponse(rawResponse);

            log.debug(
                    "AI decision response: {}",
                    jsonResponse
            );

            return objectMapper.readValue(
                    jsonResponse,
                    AgentDecision.class
            );

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

    private String cleanJsonResponse(String response) {

        String cleaned = response.trim();

        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7).trim();
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3).trim();
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(
                    0,
                    cleaned.length() - 3
            ).trim();
        }

        return cleaned;
    }
}