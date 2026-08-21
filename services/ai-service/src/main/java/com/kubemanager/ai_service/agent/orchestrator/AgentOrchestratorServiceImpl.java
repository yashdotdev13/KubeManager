package com.kubemanager.ai_service.agent.orchestrator;

import com.kubemanager.ai_service.agent.context.AgentContext;
import com.kubemanager.ai_service.agent.context.AgentContextService;
import com.kubemanager.ai_service.agent.decision.AgentDecision;
import com.kubemanager.ai_service.agent.decision.AgentDecisionService;
import com.kubemanager.ai_service.agent.decision.DecisionType;
import com.kubemanager.ai_service.agent.model.AgentRequest;
import com.kubemanager.ai_service.agent.model.AgentResponse;
import com.kubemanager.ai_service.agent.reasoning.AgentReasoningService;
import com.kubemanager.ai_service.agent.tool.ToolExecutor;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import com.kubemanager.ai_service.auth.UserContext;
import com.kubemanager.ai_service.auth.UserContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AgentOrchestratorServiceImpl
        implements AgentOrchestrator {

    private final AgentDecisionService agentDecisionService;
    private final ToolExecutor toolExecutor;
    private final AgentReasoningService agentReasoningService;
    private final AgentContextService agentContextService;

    @Override
    public AgentResponse process(AgentRequest request) {

        if (request == null
                || request.getMessage() == null
                || request.getMessage().isBlank()) {

            return AgentResponse.builder()
                    .message("Please provide a valid request.")
                    .data(null)
                    .build();
        }

        UserContext userContext =
                UserContextHolder.getRequiredContext();

        String userId =
                String.valueOf(userContext.getUserId());

        AgentContext previousContext =
                agentContextService.getContext(userId);

        AgentDecision decision =
                agentDecisionService.decide(request);

        if (decision == null
                || decision.getType() == null) {

            return AgentResponse.builder()
                    .message(
                            "Unable to determine the appropriate action."
                    )
                    .data(null)
                    .build();
        }


        if (decision.getType() == DecisionType.CHAT) {

            String response =
                    decision.getResponse() != null
                            ? decision.getResponse()
                            : "Unable to generate a response.";

            updateContext(
                    userId,
                    request,
                    previousContext,
                    null,
                    null,
                    null
            );

            return AgentResponse.builder()
                    .message(response)
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
                        .message(
                                "Tool execution returned no response."
                        )
                        .data(null)
                        .build();
            }


            if (!toolResponse.isSuccess()) {

                updateContext(
                        userId,
                        request,
                        previousContext,
                        decision.getToolName(),
                        decision.getArguments(),
                        toolResponse.getData()
                );

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

            updateContext(
                    userId,
                    request,
                    previousContext,
                    decision.getToolName(),
                    decision.getArguments(),
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

    private void updateContext(
            String userId,
            AgentRequest request,
            AgentContext previousContext,
            String toolName,
            Map<String, Object> arguments,
            Object toolResult
    ) {

        AgentContext context =
                AgentContext.builder()
                        .userId(userId)
                        .lastUserMessage(request.getMessage())
                        .lastToolName(
                                toolName != null
                                        ? toolName
                                        : previousContext != null
                                        ? previousContext.getLastToolName()
                                        : null
                        )
                        .lastToolArguments(
                                arguments != null
                                        ? arguments
                                        : previousContext != null
                                        ? previousContext.getLastToolArguments()
                                        : null
                        )
                        .lastToolResult(
                                toolResult != null
                                        ? toolResult
                                        : previousContext != null
                                        ? previousContext.getLastToolResult()
                                        : null
                        )
                        .build();

        agentContextService.updateContext(
                userId,
                context
        );
    }
}