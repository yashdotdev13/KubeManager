package com.kubemanager.ai_service.agent.planner;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kubemanager.ai_service.agent.model.AgentPlan;
import com.kubemanager.ai_service.agent.model.AgentRequest;
import com.kubemanager.ai_service.agent.tool.ToolDefinition;
import com.kubemanager.ai_service.agent.tool.ToolRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentPlannerServiceImpl implements AgentPlanner {

    private final ChatClient chatClient;
    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper;

    @Override
    public AgentPlan createPlan(AgentRequest request) {

        List<ToolDefinition> tools =
                toolRegistry.getDefinitions();

        String toolDefinitions;

        try {
            toolDefinitions =
                    objectMapper.writeValueAsString(tools);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to build AI tool definitions.",
                    exception
            );
        }

        String prompt = """
                You are the planning engine of KubeManager AI Agent.

                Your responsibility is to analyze the user's request
                and decide whether a tool must be executed.

                Available tools:
                %s

                User request:
                %s

                Rules:

                1. Use a tool when the request requires real
                   Kubernetes or KubeManager data.

                2. Do not use a tool for general Kubernetes questions.

                3. If a tool is required, select exactly one tool.

                4. Extract all required arguments from the user's request.

                5. If required information is missing, do not invent it.

                6. Return ONLY valid JSON.

                Expected JSON format:

                {
                  "requiresTool": true,
                  "toolName": "cluster_health",
                  "arguments": {
                    "clusterId": "uuid"
                  },
                  "reasoning": "short explanation"
                }

                For requests that do not require a tool:

                {
                  "requiresTool": false,
                  "toolName": null,
                  "arguments": {},
                  "reasoning": "short explanation"
                }
                """.formatted(
                toolDefinitions,
                request.getMessage()
        );

        try {

            String response = chatClient
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            return objectMapper.readValue(
                    response,
                    AgentPlan.class
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Failed to create agent plan.",
                    exception
            );
        }
    }
}