package com.kubemanager.ai_service.agent.workflow;


import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgentWorkflowResult {


    private String finalResponse;

    private String finalToolName;

    private Object finalToolResult;

    private List<ToolExecutionStep> executionSteps;

    private int executionCount;

    private boolean successful;
}
