package com.kubemanager.ai_service.agent.memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemoryExtractionServiceImpl
        implements MemoryExtractionService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Override
    public MemoryExtractionResult extract(
            String userMessage,
            String toolName,
            Object toolResult
    ) {

        if (userMessage == null
                || userMessage.isBlank()) {

            return emptyResult();
        }

        String prompt = """
                You are the memory extraction component of
                KubeManager AI.

                Your job is to determine whether the current
                interaction contains information that is worth
                remembering for future interactions with this user.

                Do NOT store every interaction.

                Store information only when it has meaningful
                future value.

                Examples of potentially useful memories:

                - User preferences.
                - Repeated Kubernetes workflows.
                - Important user-specific Kubernetes context.
                - Explicit instructions from the user that should
                  influence future interactions.
                - Long-term operational preferences.
                - Important recurring resource context.

                Do NOT store:

                - Temporary conversational phrases.
                - Greetings.
                - Generic questions.
                - One-time transient information.
                - Secrets, passwords, tokens or credentials.
                - Information that is not useful for future interactions.
                - Raw large Kubernetes responses.
                - Complete tool responses.

                CURRENT USER MESSAGE:

                %s

                TOOL USED:

                %s

                TOOL RESULT:

                %s

                Return ONLY valid JSON.

                RESPONSE FORMAT:

                {
                  "shouldRemember": true,
                  "memories": [
                    {
                      "memoryType": "PREFERENCE",
                      "content": "User prefers ...",
                      "source": "AGENT_INTERACTION"
                    }
                  ]
                }

                If nothing should be remembered:

                {
                  "shouldRemember": false,
                  "memories": []
                }

                RULES:

                - Return ONLY valid JSON.
                - Do NOT return markdown.
                - Do NOT wrap the response in ```json.
                - Do NOT add explanations.
                - Keep memories concise.
                - Each memory must represent a useful fact.
                - Never store credentials or secrets.
                - Never store authentication tokens.
                - Never store passwords.
                - Do not copy the complete tool result into memory.
                """.formatted(
                userMessage,
                toolName != null
                        ? toolName
                        : "No tool executed.",
                toolResult != null
                        ? toolResult
                        : "No tool result."
        );

        try {

            String rawResponse =
                    chatClient
                            .prompt()
                            .user(prompt)
                            .call()
                            .content();

            if (rawResponse == null
                    || rawResponse.isBlank()) {

                log.warn(
                        "Memory extraction returned an empty response."
                );

                return emptyResult();
            }

            String jsonResponse =
                    cleanJsonResponse(rawResponse);

            MemoryExtractionResult result =
                    objectMapper.readValue(
                            jsonResponse,
                            MemoryExtractionResult.class
                    );

            if (result == null) {
                return emptyResult();
            }

            if (!result.isShouldRemember()
                    || result.getMemories() == null
                    || result.getMemories().isEmpty()) {

                return emptyResult();
            }

            return result;

        } catch (Exception exception) {

            log.error(
                    "Failed to extract memory from interaction.",
                    exception
            );

            /*
             * Memory extraction must never break the
             * primary agent workflow.
             */
            return emptyResult();
        }
    }

    private MemoryExtractionResult emptyResult() {

        return MemoryExtractionResult.builder()
                .shouldRemember(false)
                .memories(java.util.List.of())
                .build();
    }

    private String cleanJsonResponse(
            String response
    ) {

        String cleaned =
                response.trim();

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