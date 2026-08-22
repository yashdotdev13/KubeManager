package com.kubemanager.ai_service.agent.context;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AgentEvidenceContext {

    private Object deploymentInformation;

    private Object podInformation;

    private Object podLogs;

    private Object events;

    private Object metrics;
}