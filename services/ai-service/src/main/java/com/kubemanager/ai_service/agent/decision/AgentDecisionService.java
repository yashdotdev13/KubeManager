package com.kubemanager.ai_service.agent.decision;

import com.kubemanager.ai_service.agent.model.AgentRequest;
import com.kubemanager.ai_service.agent.workflow.AgentWorkflowContext;

public interface AgentDecisionService {


    AgentDecision decide(AgentRequest request, AgentWorkflowContext workflowContext);
}
