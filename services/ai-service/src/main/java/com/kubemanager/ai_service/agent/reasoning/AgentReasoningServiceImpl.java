package com.kubemanager.ai_service.agent.reasoning;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgentReasoningServiceImpl implements AgentReasoningService {

    private final ChatClient chatClient;

    @Override
    public String generateFinalResponse(
            String userMessage,
            String toolName,
            Object toolResult
    ) {

        String prompt = """
                You are the reasoning engine of KubeManager AI.

                The user asked:

                %s

                The agent executed the following tool:

                Tool:
                %s

                The tool returned:

                %s

                Analyze the tool result and provide a clear,
                concise and useful response to the user.

                Rules:

                - Do not mention internal implementation details.
                - Do not mention ToolExecutor, ToolRegistry or Java classes.
                - Do not invent information that is not present in the tool result.
                - Explain important health or operational information clearly.
                - If the tool execution failed, explain that clearly.
                - Answer directly based on the actual tool result.
                """.formatted(
                userMessage,
                toolName,
                toolResult
        );

        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }
}