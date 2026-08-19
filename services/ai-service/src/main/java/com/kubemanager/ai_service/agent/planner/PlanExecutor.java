package com.kubemanager.ai_service.agent.planner;


import com.kubemanager.ai_service.agent.model.AgentPlan;
import com.kubemanager.ai_service.agent.tool.ToolExecutor;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanExecutor {


    private final ToolExecutor toolExecutor;

    public List<ToolResponse> execute(AgentPlan plan){

        List<ToolResponse> results = new ArrayList<>();

        if(plan == null || plan.getSteps()== null){
            return results;
        }

        for(AgentPlanStep step: plan.getSteps()){

            if(step.getType() != PlanStepType.TOOL_CALL){
                continue;
            }

            ToolRequest toolRequest = ToolRequest.builder()
                    .toolName(step.getToolName())
                    .arguments(step.getArguments())
                    .build();

            ToolResponse toolResponse =
                    toolExecutor.execute(toolRequest);

            results.add(toolResponse);


            /*
             * Stop execution if a tool fails.
             *
             * Later, we will make this more intelligent so the
             * agent can decide what to do after a failed step.
             */
            if (!toolResponse.isSuccess()) {
                break;
            }
        }
        return results;
    }
}
