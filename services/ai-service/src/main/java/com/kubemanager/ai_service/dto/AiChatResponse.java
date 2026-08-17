package com.kubemanager.ai_service.dto;


import java.util.UUID;

public record AiChatResponse(
        UUID userId,
        String response
) {
}