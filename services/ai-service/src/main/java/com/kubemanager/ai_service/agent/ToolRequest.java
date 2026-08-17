package com.kubemanager.ai_service.agent;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ToolRequest {

    private String toolName;

    private Map<String, Object> arguments;
}