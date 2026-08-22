package com.kubemanager.ai_service.agent.orchestrator;

import com.kubemanager.ai_service.agent.context.AgentContext;
import com.kubemanager.ai_service.agent.context.AgentContextService;
import com.kubemanager.ai_service.agent.memory.AgentMemoryService;
import com.kubemanager.ai_service.agent.memory.MemoryExtractionResult;
import com.kubemanager.ai_service.agent.memory.MemoryExtractionService;
import com.kubemanager.ai_service.agent.model.AgentRequest;
import com.kubemanager.ai_service.agent.model.AgentResponse;
import com.kubemanager.ai_service.agent.workflow.AgentWorkflowExecutor;
import com.kubemanager.ai_service.agent.workflow.AgentWorkflowResult;
import com.kubemanager.ai_service.agent.workflow.ToolExecutionStep;
import com.kubemanager.ai_service.auth.UserContext;
import com.kubemanager.ai_service.auth.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
        AgentWorkflowResult workflowResult;

        try {

            workflowResult =
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

        if (workflowResult == null) {

            return AgentResponse.builder()
                    .message(
                            "The agent workflow returned no result."
                    )
                    .data(null)
                    .build();
        }
        updateAgentContext(
                userId,
                request,
                previousContext,
                workflowResult
        );
        extractAndStoreMemorySafely(
                userId,
                request.getMessage(),
                workflowResult
        );

        String finalResponse =
                workflowResult.getFinalResponse();

        if (finalResponse == null
                || finalResponse.isBlank()) {

            finalResponse =
                    workflowResult.isSuccessful()
                            ? "The operation completed successfully."
                            : "The requested operation could not be completed.";
        }

        return AgentResponse.builder()
                .message(finalResponse)
                .data(
                        workflowResult.getFinalToolResult()
                )
                .build();
    }
    private void updateAgentContext(
            String userId,
            AgentRequest request,
            AgentContext previousContext,
            AgentWorkflowResult workflowResult
    ) {

        String lastToolName =
                workflowResult.getFinalToolName();

        Map<String, Object> lastToolArguments =
                extractLastToolArguments(
                        workflowResult
                );

        Object lastToolResult =
                workflowResult.getFinalToolResult();

        /*
         * Preserve previous information when the workflow
         * did not execute a tool.
         */
        if (lastToolName == null
                && previousContext != null) {

            lastToolName =
                    previousContext.getLastToolName();
        }

        if (lastToolArguments == null
                && previousContext != null) {

            lastToolArguments =
                    previousContext.getLastToolArguments();
        }

        if (lastToolResult == null
                && previousContext != null) {

            lastToolResult =
                    previousContext.getLastToolResult();
        }

        AgentContext context =
                AgentContext.builder()
                        .userId(userId)
                        .lastUserMessage(
                                request.getMessage()
                        )
                        .lastToolName(
                                lastToolName
                        )
                        .lastToolArguments(
                                lastToolArguments
                        )
                        .lastToolResult(
                                lastToolResult
                        )
                        .build();

        agentContextService.updateContext(
                userId,
                context
        );
    }

    private Map<String, Object> extractLastToolArguments(
            AgentWorkflowResult workflowResult
    ) {

        List<ToolExecutionStep> executionSteps =
                workflowResult.getExecutionSteps();

        if (executionSteps == null
                || executionSteps.isEmpty()) {

            return null;
        }

        ToolExecutionStep lastStep =
                executionSteps.get(
                        executionSteps.size() - 1
                );

        return lastStep.getArguments();
    }
    private void extractAndStoreMemorySafely(
            String userId,
            String userMessage,
            AgentWorkflowResult workflowResult
    ) {

        try {

            /*
             * Pass the workflow result to memory extraction
             * instead of only passing the final response.
             *
             * This allows memory extraction to consider the
             * complete agent interaction.
             */
            MemoryExtractionResult extractionResult =
                    memoryExtractionService.extract(
                            userMessage,
                            workflowResult.getFinalToolName(),
                            workflowResult.getFinalToolResult()
                    );

            if (extractionResult == null) {

                return;
            }

            if (!extractionResult.isShouldRemember()) {

                log.debug(
                        "No persistent memory identified " +
                                "for userId={}",
                        userId
                );

                return;
            }

            if (extractionResult.getMemories() == null
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

                    log.warn(
                            "Skipping memory with missing type."
                    );

                    continue;
                }

                if (memory.getContent() == null
                        || memory.getContent().isBlank()) {

                    log.warn(
                            "Skipping memory with empty content."
                    );

                    continue;
                }

                agentMemoryService.saveMemory(
                        userId,
                        memory.getMemoryType(),
                        memory.getContent(),
                        memory.getSource()
                );

                log.debug(
                        "Persistent memory saved for userId={} " +
                                "type={}",
                        userId,
                        memory.getMemoryType()
                );
            }

        } catch (Exception exception) {

            /*
             * Memory is an auxiliary capability.
             *
             * A memory failure must NEVER cause a Kubernetes
             * workflow to fail.
             */
            log.error(
                    "Failed to extract/store agent memory " +
                            "for userId={}",
                    userId,
                    exception
            );
        }
    }
}