package com.kubemanager.ai_service.agent;

import com.kubemanager.ai_service.agent.tool.ToolDefinition;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;

public interface AiTool {


    String getName();

    ToolDefinition getToolDefinition();

    ToolResponse execute(ToolRequest request);
}
