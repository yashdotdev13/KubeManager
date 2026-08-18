package com.kubemanager.ai_service.agent.planner;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kubemanager.ai_service.agent.model.AgentPlan;
import com.kubemanager.ai_service.agent.model.AgentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentPlannerServiceImpl implements AgentPlanner {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Override
    public AgentPlan createPlan(AgentRequest request) {

        String prompt = """
                You are the planning engine of KubeManager AI.

                Your responsibility is to create an execution plan
                for the user's Kubernetes-related request.

                AVAILABLE TOOLS:

                1. cluster_health

                Description:
                Checks the health status of a Kubernetes cluster.

                Input:
                clusterId - UUID of the Kubernetes cluster.

                PLAN FORMAT:

                Return ONLY valid JSON.

                Example:

                {
                  "steps": [
                    {
                      "step": 1,
                      "type": "TOOL_CALL",
                      "toolName": "cluster_health",
                      "arguments": {
                        "clusterId": "UUID"
                      },
                      "description": "Check the health of the cluster"
                    }
                  ]
                }

                RULES:

                - Return ONLY JSON.
                - Do not return markdown.
                - Do not invent tool names.
                - Do not invent cluster IDs.
                - Only use available tools.
                - Use TOOL_CALL when a tool is required.
                - If the request does not require a tool, return an empty plan.
                - Keep the plan minimal.
                - Do not create unnecessary steps.

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
                        "AI planner returned an empty response"
                );
            }

            String jsonResponse = cleanJsonResponse(rawResponse);

            log.debug(
                    "AI generated execution plan: {}",
                    jsonResponse
            );

            return objectMapper.readValue(
                    jsonResponse,
                    AgentPlan.class
            );

        } catch (Exception exception) {

            log.error(
                    "Failed to generate execution plan",
                    exception
            );

            throw new IllegalStateException(
                    "Failed to generate agent execution plan",
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