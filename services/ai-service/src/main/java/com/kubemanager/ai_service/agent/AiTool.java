package com.kubemanager.ai_service.agent;

public interface AiTool {


    String getName();

    ToolDefinition getToolDefinition();

    ToolResponse execute(ToolRequest request);
}
