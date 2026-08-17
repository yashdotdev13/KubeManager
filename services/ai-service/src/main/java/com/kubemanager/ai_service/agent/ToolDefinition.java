package com.kubemanager.ai_service.agent;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ToolDefinition {

    private String name;

    private String description;

    private String inputSchema;
}