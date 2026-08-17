package com.kubemanager.ai_service.service.Impl;

import com.kubemanager.ai_service.auth.UserContext;
import com.kubemanager.ai_service.auth.UserContextHolder;

import com.kubemanager.ai_service.dto.AiChatRequest;
import com.kubemanager.ai_service.dto.AiChatResponse;
import com.kubemanager.ai_service.exceptions.AiContextException;
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

        UserContext userContext;

        try {
            userContext = UserContextHolder.getRequiredContext();
        } catch (IllegalStateException exception) {
            throw new AiContextException(
                    "Authenticated user context is not available",
                    exception
            );
        }

        try {

            String response = chatClient
                    .prompt()
                    .user(request.message())
                    .call()
                    .content();

            return new AiChatResponse(
                    userContext.getUserId(),
                    response
            );

        } catch (Exception exception) {

            throw new AiModelException(
                    "Failed to communicate with AI model",
                    exception
            );
        }
    }
}