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

        if (request == null || request.getMessage() == null
                || request.getMessage().isBlank()) {

            return AgentResponse.builder()
                    .message("Please provide a valid request.")
                    .data(null)
                    .build();
        }
        AgentDecision decision =
                agentDecisionService.decide(request);

        if (decision == null || decision.getType() == null) {

            return AgentResponse.builder()
                    .message(
                            "Unable to determine the appropriate action."
                    )
                    .data(null)
                    .build();
        }
        if (decision.getType() == DecisionType.CHAT) {

            return AgentResponse.builder()
                    .message(
                            decision.getResponse() != null
                                    ? decision.getResponse()
                                    : "Unable to generate a response."
                    )
                    .data(null)
                    .build();
        }
        if (decision.getType() == DecisionType.TOOL_CALL) {

            if (decision.getToolName() == null
                    || decision.getToolName().isBlank()) {

                return AgentResponse.builder()
                        .message("No valid tool was selected.")
                        .data(null)
                        .build();
            }

            ToolRequest toolRequest =
                    ToolRequest.builder()
                            .toolName(decision.getToolName())
                            .arguments(decision.getArguments())
                            .build();

            ToolResponse toolResponse =
                    toolExecutor.execute(toolRequest);
            if (toolResponse == null) {

                return AgentResponse.builder()
                        .message("Tool execution returned no response.")
                        .data(null)
                        .build();
            }

            if (!toolResponse.isSuccess()) {

                return AgentResponse.builder()
                        .message(
                                toolResponse.getMessage() != null
                                        ? toolResponse.getMessage()
                                        : "Tool execution failed."
                        )
                        .data(toolResponse.getData())
                        .build();
            }
            String finalResponse =
                    agentReasoningService.generateFinalResponse(
                            request.getMessage(),
                            decision.getToolName(),
                            toolResponse.getData()
                    );
            return AgentResponse.builder()
                    .message(
                            finalResponse != null
                                    ? finalResponse
                                    : "The operation completed successfully."
                    )
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