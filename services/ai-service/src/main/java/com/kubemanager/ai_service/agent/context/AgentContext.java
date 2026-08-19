package com.kubemanager.ai_service.agent.context;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class AgentContext {

    private String lastUserMessage;

    private String lastToolName;

    private Map<String, Object> lastToolArguments;

    private Object lastToolResult;
}