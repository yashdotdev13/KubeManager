package com.kubemanager.ai_service.agent.chat;


import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final ChatClient chatClient;


    @Override
    public String chat(String message) {

        return chatClient
                .prompt()
                .user(message)
                .call()
                .content();
    }
}
