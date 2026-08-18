package com.kubemanager.ai_service.agent.decision;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kubemanager.ai_service.agent.model.AgentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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

                Available tool:

                TOOL NAME:
                cluster_health

                DESCRIPTION:
                Checks the health status of a Kubernetes cluster.

                TOOL INPUT:
                clusterId - UUID of the Kubernetes cluster.

                Return ONLY valid JSON.

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

                Important:
                - Do not add markdown.
                - Do not add explanations outside the JSON.
                - Do not invent a clusterId.
                - If the user asks for cluster health but does not provide
                  a clusterId, return CHAT and ask the user for the clusterId.

                User request:
                %s
                """.formatted(request.getMessage());

        try {

            String response = chatClient
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            return objectMapper.readValue(
                    response,
                    AgentDecision.class
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Failed to generate agent decision",
                    exception
            );
        }
    }
}