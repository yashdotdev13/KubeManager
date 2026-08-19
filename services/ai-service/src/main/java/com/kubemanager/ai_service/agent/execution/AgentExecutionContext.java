package com.kubemanager.ai_service.agent.execution;


import com.kubemanager.ai_service.agent.model.AgentPlan;
import com.kubemanager.ai_service.agent.model.AgentRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AgentExecutionContext {

    private AgentRequest request;
    private AgentPlan plan;

    private List<ToolResponse> toolResults;
}
