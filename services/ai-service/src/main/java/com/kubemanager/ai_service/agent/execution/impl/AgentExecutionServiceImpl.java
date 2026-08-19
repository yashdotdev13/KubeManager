package com.kubemanager.ai_service.agent.execution.impl;

import com.kubemanager.ai_service.agent.execution.AgentExecutionContext;
import com.kubemanager.ai_service.agent.execution.AgentExecutionService;
import com.kubemanager.ai_service.agent.model.AgentPlan;
import com.kubemanager.ai_service.agent.model.AgentRequest;
import com.kubemanager.ai_service.agent.planner.AgentPlanner;
import com.kubemanager.ai_service.agent.planner.PlanExecutor;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentExecutionServiceImpl
        implements AgentExecutionService {

    private final AgentPlanner agentPlanner;
    private final PlanExecutor planExecutor;

    @Override
    public AgentExecutionContext execute(
            AgentRequest request
    ) {

        AgentPlan plan =
                agentPlanner.createPlan(request);

        List<ToolResponse> toolResults =
                planExecutor.execute(plan);

        return AgentExecutionContext.builder()
                .request(request)
                .plan(plan)
                .toolResults(toolResults)
                .build();
    }


}