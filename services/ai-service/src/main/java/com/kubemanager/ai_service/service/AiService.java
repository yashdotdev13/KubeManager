package com.kubemanager.ai_service.service;

import com.kubemanager.ai_service.dto.AiChatRequest;
import com.kubemanager.ai_service.dto.AiChatResponse;

public interface  AiService {

    AiChatResponse chat(AiChatRequest request);
}
