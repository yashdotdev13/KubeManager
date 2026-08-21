package com.kubemanager.ai_service.agent.workflow;

import com.kubemanager.ai_service.agent.tool.ToolExecutor;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentWorkflowExecutor {

    private static final int MAX_WORKFLOW_STEPS = 8;

    private final WorkflowDecisionService workflowDecisionService;
    private final ToolExecutor toolExecutor;

    public WorkflowDecision execute(
            String userMessage
    ) {

        if (userMessage == null
                || userMessage.isBlank()) {

            throw new IllegalArgumentException(
                    "User message cannot be null or blank."
            );
        }

        AgentWorkflowContext workflowContext =
                new AgentWorkflowContext(
                        userMessage
                );

        while (
                workflowContext.getExecutionCount()
                        < MAX_WORKFLOW_STEPS
        ) {

            WorkflowDecision decision =
                    workflowDecisionService.decide(
                            workflowContext
                    );

            if (decision == null
                    || decision.getType() == null) {

                throw new IllegalStateException(
                        "Workflow decision cannot be null."
                );
            }

            if (decision.getType()
                    == WorkflowDecisionType.COMPLETE) {

                log.info(
                        "Agent workflow completed after {} steps.",
                        workflowContext.getExecutionCount()
                );

                return decision;
            }

            if (decision.getType()
                    != WorkflowDecisionType.EXECUTE_TOOL) {

                throw new IllegalStateException(
                        "Unsupported workflow decision type: "
                                + decision.getType()
                );
            }

            if (decision.getToolName() == null
                    || decision.getToolName().isBlank()) {

                throw new IllegalStateException(
                        "Workflow selected an invalid tool."
                );
            }

            ToolRequest toolRequest =
                    ToolRequest.builder()
                            .toolName(
                                    decision.getToolName()
                            )
                            .arguments(
                                    decision.getArguments()
                            )
                            .build();

            log.info(
                    "Executing workflow tool '{}' at step {}.",
                    decision.getToolName(),
                    workflowContext.getExecutionCount() + 1
            );

            ToolResponse toolResponse =
                    toolExecutor.execute(
                            toolRequest
                    );

            if (toolResponse == null) {

                throw new IllegalStateException(
                        "Tool execution returned no response."
                );
            }

            int stepNumber =
                    workflowContext.getExecutionCount() + 1;

            ToolExecutionStep executionStep =
                    ToolExecutionStep.builder()
                            .stepNumber(stepNumber)
                            .toolName(
                                    decision.getToolName()
                            )
                            .arguments(
                                    decision.getArguments()
                            )
                            .response(
                                    toolResponse
                            )
                            .build();

            workflowContext.addExecutionStep(
                    executionStep
            );

            workflowContext.addToolResult(
                    decision.getToolName(),
                    toolResponse
            );
            if (!toolResponse.isSuccess()) {

                log.warn(
                        "Workflow tool '{}' failed at step {}.",
                        decision.getToolName(),
                        stepNumber
                );

                return WorkflowDecision.builder()
                        .type(
                                WorkflowDecisionType.COMPLETE
                        )
                        .response(
                                toolResponse.getMessage() != null
                                        ? toolResponse.getMessage()
                                        : "The requested operation could not be completed."
                        )
                        .reasoning(
                                "Workflow stopped because the selected tool failed."
                        )
                        .build();
            }
        }

        log.warn(
                "Workflow reached maximum step limit of {}.",
                MAX_WORKFLOW_STEPS
        );

        return WorkflowDecision.builder()
                .type(
                        WorkflowDecisionType.COMPLETE
                )
                .response(
                        "I could not safely complete the requested " +
                                "operation within the allowed workflow steps."
                )
                .reasoning(
                        "Maximum workflow execution limit reached."
                )
                .build();
    }
}