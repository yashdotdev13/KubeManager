package com.kubemanager.ai_service.agent.planner;

import com.kubemanager.ai_service.agent.model.AgentPlan;
import com.kubemanager.ai_service.agent.tool.ToolExecutor;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import com.kubemanager.ai_service.agent.workflow.AgentWorkflowContext;
import com.kubemanager.ai_service.agent.workflow.ToolExecutionStep;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanExecutor {

    private final ToolExecutor toolExecutor;

    public List<ToolResponse> execute(
            AgentPlan plan,
            AgentWorkflowContext context
    ) {

        List<ToolResponse> results =
                new ArrayList<>();

        if (plan == null
                || plan.getSteps() == null
                || context == null) {

            return results;
        }

        for (AgentPlanStep step : plan.getSteps()) {

            if (step == null) {
                continue;
            }

            if (step.getType() != PlanStepType.TOOL_CALL) {
                continue;
            }

            String toolName =
                    step.getToolName();

            if (toolName == null
                    || toolName.isBlank()) {

                log.warn(
                        "Skipping plan step with missing tool name."
                );

                continue;
            }

            ToolRequest toolRequest =
                    ToolRequest.builder()
                            .toolName(toolName)
                            .arguments(step.getArguments())
                            .build();

            log.info(
                    "Executing plan step {}: {}",
                    step.getStep(),
                    toolName
            );

            ToolResponse toolResponse =
                    toolExecutor.execute(
                            toolRequest
                    );

            results.add(toolResponse);

            /*
             * Store execution information in workflow context.
             */
            context.addExecutionStep(
                    ToolExecutionStep.builder()
                            .toolName(toolName)
                            .build()
            );

            /*
             * Store the tool result so that
             * later agent reasoning can use it.
             */
            context.addToolResult(
                    toolName,
                    toolResponse
            );

            /*
             * Stop execution if the tool itself
             * reports a failure.
             *
             * Later the agent can decide how
             * to recover from failures.
             */
            if (!toolResponse.isSuccess()) {

                log.warn(
                        "Tool '{}' failed. Stopping plan execution.",
                        toolName
                );

                break;
            }
        }

        return results;
    }
}