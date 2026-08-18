package com.kubemanager.ai_service.agent.orchestrator;

import com.kubemanager.ai_service.agent.model.AgentRequest;
import com.kubemanager.ai_service.agent.model.AgentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AgentOrchestratorServiceImpl implements AgentOrchestrator{


    @Override
    public AgentResponse process(AgentRequest request) {


        return AgentResponse.builder()
                .message("Agent processing is not implemented yet.")
                .build();
    }
}
