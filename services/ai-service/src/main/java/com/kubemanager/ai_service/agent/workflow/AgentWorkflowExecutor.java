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

    public AgentWorkflowResult execute(
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

                return AgentWorkflowResult.builder()
                        .finalResponse(
                                decision.getResponse()
                        )
                        .finalToolName(
                                getLastToolName(
                                        workflowContext
                                )
                        )
                        .finalToolResult(
                                getLastToolResult(
                                        workflowContext
                                )
                        )
                        .executionSteps(
                                workflowContext.getExecutionSteps()
                        )
                        .executionCount(
                                workflowContext.getExecutionCount()
                        )
                        .successful(true)
                        .build();
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

            if (decision.getArguments() == null) {

                throw new IllegalStateException(
                        "Workflow tool arguments cannot be null."
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

            int stepNumber =
                    workflowContext.getExecutionCount() + 1;

            log.info(
                    "Executing workflow tool '{}' at step {}.",
                    decision.getToolName(),
                    stepNumber
            );

            ToolResponse toolResponse =
                    toolExecutor.execute(
                            toolRequest
                    );

            if (toolResponse == null) {

                log.error(
                        "Tool '{}' returned null response.",
                        decision.getToolName()
                );

                return AgentWorkflowResult.builder()
                        .finalResponse(
                                "Tool execution returned no response."
                        )
                        .finalToolName(
                                decision.getToolName()
                        )
                        .executionSteps(
                                workflowContext.getExecutionSteps()
                        )
                        .executionCount(
                                workflowContext.getExecutionCount()
                        )
                        .successful(false)
                        .build();
            }
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

                return AgentWorkflowResult.builder()
                        .finalResponse(
                                toolResponse.getMessage() != null
                                        ? toolResponse.getMessage()
                                        : "The requested operation could not be completed."
                        )
                        .finalToolName(
                                decision.getToolName()
                        )
                        .finalToolResult(
                                toolResponse.getData()
                        )
                        .executionSteps(
                                workflowContext.getExecutionSteps()
                        )
                        .executionCount(
                                workflowContext.getExecutionCount()
                        )
                        .successful(false)
                        .build();
            }
        }
        log.warn(
                "Workflow reached maximum step limit of {}.",
                MAX_WORKFLOW_STEPS
        );

        return AgentWorkflowResult.builder()
                .finalResponse(
                        "I could not safely complete the " +
                                "requested operation within the " +
                                "allowed workflow steps."
                )
                .finalToolName(
                        getLastToolName(
                                workflowContext
                        )
                )
                .finalToolResult(
                        getLastToolResult(
                                workflowContext
                        )
                )
                .executionSteps(
                        workflowContext.getExecutionSteps()
                )
                .executionCount(
                        workflowContext.getExecutionCount()
                )
                .successful(false)
                .build();
    }
    private String getLastToolName(
            AgentWorkflowContext workflowContext
    ) {

        if (workflowContext.getExecutionSteps() == null
                || workflowContext.getExecutionSteps().isEmpty()) {

            return null;
        }

        return workflowContext
                .getExecutionSteps()
                .get(
                        workflowContext
                                .getExecutionSteps()
                                .size() - 1
                )
                .getToolName();
    }

    private Object getLastToolResult(
            AgentWorkflowContext workflowContext
    ) {

        if (workflowContext.getExecutionSteps() == null
                || workflowContext.getExecutionSteps().isEmpty()) {

            return null;
        }

        ToolResponse response =
                workflowContext
                        .getExecutionSteps()
                        .get(
                                workflowContext
                                        .getExecutionSteps()
                                        .size() - 1
                        )
                        .getResponse();

        if (response == null) {
            return null;
        }
        return response.getData();
    }
}