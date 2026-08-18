package com.kubemanager.ai_service.agent.orchestrator;

import com.kubemanager.ai_service.agent.decision.AgentDecision;
import com.kubemanager.ai_service.agent.decision.AgentDecisionService;
import com.kubemanager.ai_service.agent.decision.DecisionType;
import com.kubemanager.ai_service.agent.model.AgentRequest;
import com.kubemanager.ai_service.agent.model.AgentResponse;
import com.kubemanager.ai_service.agent.reasoning.AgentReasoningService;
import com.kubemanager.ai_service.agent.tool.ToolExecutor;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgentOrchestratorServiceImpl implements AgentOrchestrator {


    private final AgentDecisionService agentDecisionService;
    private final ToolExecutor toolExecutor;
    private final AgentReasoningService agentReasoningService;

    @Override
    public AgentResponse process(AgentRequest request) {

        AgentDecision decision =
                agentDecisionService.decide(request);

        if (decision.getType() == DecisionType.CHAT) {

            return AgentResponse.builder()
                    .message(decision.getResponse())
                    .data(null)
                    .build();
        }

        if (decision.getType() == DecisionType.TOOL_CALL) {

            ToolRequest toolRequest = ToolRequest.builder()
                    .toolName(decision.getToolName())
                    .arguments(decision.getArguments())
                    .build();

            ToolResponse toolResponse =
                    toolExecutor.execute(toolRequest);

            String finalResponse =
                    agentReasoningService.generateFinalResponse(
                            request.getMessage(),
                            decision.getToolName(),
                            toolResponse.getData()
                    );

            return AgentResponse.builder()
                    .message(finalResponse)
                    .data(toolResponse.getData())
                    .build();
        }

        return AgentResponse.builder()
                .message(
                        "Unable to determine the appropriate action."
                )
                .data(null)
                .build();
    }
}