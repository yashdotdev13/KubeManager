package com.kubemanager.ai_service.agent.orchestrator;


import com.kubemanager.ai_service.agent.decision.AgentDecision;
import com.kubemanager.ai_service.agent.decision.AgentDecisionService;
import com.kubemanager.ai_service.agent.decision.DecisionType;
import com.kubemanager.ai_service.agent.model.AgentRequest;
import com.kubemanager.ai_service.agent.model.AgentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgentOrchestratorServiceImpl implements AgentOrchestrator {

    private final AgentDecisionService agentDecisionService;

    @Override
    public AgentResponse process(AgentRequest request) {

        AgentDecision decision =
                agentDecisionService.decide(request);

        if (decision.getType() == DecisionType.CHAT) {

            return AgentResponse.builder()
                    .message(decision.getResponse())
                    .build();
        }

        return AgentResponse.builder()
                .message(
                        "Agent decided to execute tool: "
                                + decision.getToolName()
                )
                .build();
    }
}