package com.kubemanager.ai_service.service.Impl;


import com.kubemanager.ai_service.dto.AiChatRequest;
import com.kubemanager.ai_service.dto.AiChatResponse;
import com.kubemanager.ai_service.exceptions.AiModelException;
import com.kubemanager.ai_service.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final ChatClient chatClient;

    @Override
    public AiChatResponse chat(AiChatRequest request) {

        try {

            String response = chatClient
                    .prompt()
                    .user(request.message())
                    .call()
                    .content();

            return new AiChatResponse(response);

        } catch (Exception exception) {

            throw new AiModelException(
                    "Failed to communicate with AI model",
                    exception
            );
        }
    }
}
