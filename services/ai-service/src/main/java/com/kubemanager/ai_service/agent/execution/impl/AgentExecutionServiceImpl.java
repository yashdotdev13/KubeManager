package com.kubemanager.ai_service.agent.execution.impl;

import com.kubemanager.ai_service.agent.execution.AgentExecutionContext;
import com.kubemanager.ai_service.agent.execution.AgentExecutionService;
import com.kubemanager.ai_service.agent.model.AgentPlan;
import com.kubemanager.ai_service.agent.model.AgentRequest;
import com.kubemanager.ai_service.agent.planner.AgentPlanner;
import com.kubemanager.ai_service.agent.planner.PlanExecutor;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import com.kubemanager.ai_service.agent.workflow.AgentWorkflowContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentExecutionServiceImpl
        implements AgentExecutionService {

    private final AgentPlanner agentPlanner;

    private final PlanExecutor planExecutor;

    @Override
    public AgentExecutionContext execute(
            AgentRequest request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Agent request cannot be null."
            );
        }

        if (request.getMessage() == null
                || request.getMessage().isBlank()) {

            throw new IllegalArgumentException(
                    "Agent request message cannot be null or blank."
            );
        }

        /*
         * Create the workflow context before executing
         * the generated plan.
         *
         * This context stores the execution state and
         * tool results produced by the plan.
         */
        AgentWorkflowContext workflowContext =
                new AgentWorkflowContext(
                        request.getMessage()
                );

        log.info(
                "Creating agent execution plan for request."
        );

        /*
         * Step 1:
         * Ask the planner to create an execution plan.
         */
        AgentPlan plan =
                agentPlanner.createPlan(
                        request
                );

        if (plan == null) {

            throw new IllegalStateException(
                    "Agent planner returned a null plan."
            );
        }

        log.info(
                "Agent execution plan created successfully."
        );

        /*
         * Step 2:
         * Execute the generated plan.
         *
         * PlanExecutor requires both:
         * - AgentPlan
         * - AgentWorkflowContext
         */
        List<ToolResponse> toolResults =
                planExecutor.execute(
                        plan,
                        workflowContext
                );

        log.info(
                "Agent plan execution completed. " +
                        "toolResults={}",
                toolResults.size()
        );

        /*
         * Step 3:
         * Return the complete execution context.
         */
        return AgentExecutionContext.builder()
                .request(request)
                .plan(plan)
                .toolResults(toolResults)
                .build();
    }
}