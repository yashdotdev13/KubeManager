package com.kubemanager.ai_service.agent.reasoning.rca;


import com.kubemanager.ai_service.agent.workflow.AgentWorkflowContext;

public interface RootCauseAnalysisService {

    RootCauseAnalysis analyze(
            String userRequest,
            AgentWorkflowContext workflowContext
    );
}