package com.kubemanager.ai_service.agent.workflow;

import com.kubemanager.ai_service.agent.tool.ToolResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ToolExecutionStep {

    private final int stepNumber;

    private final String toolName;

    private final Map<String, Object> arguments;

    private final ToolResponse response;
}