package com.kubemanager.ai_service.agent.workflow;

public interface WorkflowDecisionService {

    WorkflowDecision decide(
            AgentWorkflowContext workflowContext
    );
}