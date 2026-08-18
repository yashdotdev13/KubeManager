package com.kubemanager.ai_service.agent.orchestrator;

import com.kubemanager.ai_service.agent.chat.AiChatService;
import com.kubemanager.ai_service.agent.model.AgentRequest;
import com.kubemanager.ai_service.agent.model.AgentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AgentOrchestratorServiceImpl implements AgentOrchestrator{


    private final AiChatService aiChatService;


    @Override
    public AgentResponse process(AgentRequest request) {


        String response = aiChatService.chat(request.getMessage());

        return AgentResponse.builder()
                .message(response)
                .build();
    }
}
