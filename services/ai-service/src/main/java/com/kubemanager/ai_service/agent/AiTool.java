package com.kubemanager.ai_service.agent;

import com.google.genai.types.ToolResponse;
import org.springframework.ai.tool.definition.ToolDefinition;

public interface AiTool {


    String getName();

    ToolDefinition getToolDefinition();

    ToolResponse execute(ToolReq)
}
