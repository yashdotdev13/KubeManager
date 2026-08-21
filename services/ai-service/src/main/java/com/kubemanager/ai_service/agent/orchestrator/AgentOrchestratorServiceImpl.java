package com.kubemanager.ai_service.agent.orchestrator;

import com.kubemanager.ai_service.agent.context.AgentContext;
import com.kubemanager.ai_service.agent.context.AgentContextService;
import com.kubemanager.ai_service.agent.memory.AgentMemoryService;
import com.kubemanager.ai_service.agent.memory.MemoryExtractionResult;
import com.kubemanager.ai_service.agent.memory.MemoryExtractionService;
import com.kubemanager.ai_service.agent.model.AgentRequest;
import com.kubemanager.ai_service.agent.model.AgentResponse;
import com.kubemanager.ai_service.agent.workflow.AgentWorkflowExecutor;
import com.kubemanager.ai_service.agent.workflow.WorkflowDecision;
import com.kubemanager.ai_service.agent.workflow.WorkflowDecisionType;
import com.kubemanager.ai_service.auth.UserContext;
import com.kubemanager.ai_service.auth.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentOrchestratorServiceImpl
        implements AgentOrchestrator {

    private final AgentWorkflowExecutor agentWorkflowExecutor;
    private final AgentContextService agentContextService;
    private final MemoryExtractionService memoryExtractionService;
    private final AgentMemoryService agentMemoryService;

    @Override
    public AgentResponse process(
            AgentRequest request
    ) {

        if (request == null
                || request.getMessage() == null
                || request.getMessage().isBlank()) {

            return AgentResponse.builder()
                    .message(
                            "Please provide a valid request."
                    )
                    .data(null)
                    .build();
        }

        UserContext userContext =
                UserContextHolder.getRequiredContext();

        String userId =
                String.valueOf(
                        userContext.getUserId()
                );
        AgentContext previousContext =
                agentContextService.getContext(
                        userId
                );

        WorkflowDecision workflowDecision;

        try {

            workflowDecision =
                    agentWorkflowExecutor.execute(
                            request.getMessage()
                    );

        } catch (Exception exception) {

            log.error(
                    "Agent workflow execution failed " +
                            "for userId={}",
                    userId,
                    exception
            );

            return AgentResponse.builder()
                    .message(
                            "I was unable to complete the " +
                                    "requested operation."
                    )
                    .data(null)
                    .build();
        }

        if (workflowDecision == null) {

            return AgentResponse.builder()
                    .message(
                            "The agent workflow returned no response."
                    )
                    .data(null)
                    .build();
        }


        if (workflowDecision.getType()
                != WorkflowDecisionType.COMPLETE) {

            return AgentResponse.builder()
                    .message(
                            "The agent workflow did not complete safely."
                    )
                    .data(null)
                    .build();
        }

        String finalResponse =
                workflowDecision.getResponse();

        updateAgentContext(
                userId,
                request,
                previousContext,
                workflowDecision
        );

        extractAndStoreMemorySafely(
                userId,
                request.getMessage(),
                null,
                workflowDecision.getResponse()
        );

        return AgentResponse.builder()
                .message(
                        finalResponse != null
                                && !finalResponse.isBlank()
                                ? finalResponse
                                : "The operation completed successfully."
                )
                .data(null)
                .build();
    }
    private void updateAgentContext(
            String userId,
            AgentRequest request,
            AgentContext previousContext,
            WorkflowDecision workflowDecision
    ) {

        AgentContext context =
                AgentContext.builder()
                        .userId(userId)
                        .lastUserMessage(
                                request.getMessage()
                        )
                        .lastToolName(
                                previousContext != null
                                        ? previousContext.getLastToolName()
                                        : null
                        )
                        .lastToolArguments(
                                previousContext != null
                                        ? previousContext.getLastToolArguments()
                                        : null
                        )
                        .lastToolResult(
                                workflowDecision.getResponse()
                        )
                        .build();

        agentContextService.updateContext(
                userId,
                context
        );
    }

    private void extractAndStoreMemorySafely(
            String userId,
            String userMessage,
            String toolName,
            Object toolResult
    ) {

        try {

            MemoryExtractionResult extractionResult =
                    memoryExtractionService.extract(
                            userMessage,
                            toolName,
                            toolResult
                    );

            if (extractionResult == null
                    || !extractionResult.isShouldRemember()
                    || extractionResult.getMemories() == null
                    || extractionResult.getMemories().isEmpty()) {

                return;
            }

            for (
                    MemoryExtractionResult.MemoryItem memory
                    : extractionResult.getMemories()
            ) {

                if (memory == null) {
                    continue;
                }

                if (memory.getMemoryType() == null
                        || memory.getMemoryType().isBlank()) {

                    continue;
                }

                if (memory.getContent() == null
                        || memory.getContent().isBlank()) {

                    continue;
                }

                agentMemoryService.saveMemory(
                        userId,
                        memory.getMemoryType(),
                        memory.getContent(),
                        memory.getSource()
                );
            }

        } catch (Exception exception) {
            log.error(
                    "Failed to extract/store memory " +
                            "for userId={}",
                    userId,
                    exception
            );
        }
    }
}