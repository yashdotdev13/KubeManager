package com.kubemanager.ai_service.agent.context;

import org.springframework.stereotype.Service;

@Service
public class AgentEvidenceContextServiceImpl
        implements AgentEvidenceContextService {

    @Override
    public AgentEvidenceContext update(
            AgentEvidenceContext currentContext,
            String toolName,
            Object toolResult
    ) {

        AgentEvidenceContext current =
                currentContext != null
                        ? currentContext
                        : AgentEvidenceContext.builder()
                        .build();

        if (toolName == null
                || toolName.isBlank()) {

            return current;
        }

        return switch (toolName) {

            case "deployment_info" ->
                    AgentEvidenceContext.builder()
                            .deploymentInformation(toolResult)
                            .podInformation(
                                    current.getPodInformation()
                            )
                            .podLogs(
                                    current.getPodLogs()
                            )
                            .events(
                                    current.getEvents()
                            )
                            .metrics(
                                    current.getMetrics()
                            )
                            .build();

            case "pod_info" ->
                    AgentEvidenceContext.builder()
                            .deploymentInformation(
                                    current.getDeploymentInformation()
                            )
                            .podInformation(toolResult)
                            .podLogs(
                                    current.getPodLogs()
                            )
                            .events(
                                    current.getEvents()
                            )
                            .metrics(
                                    current.getMetrics()
                            )
                            .build();

            case "pod_logs" ->
                    AgentEvidenceContext.builder()
                            .deploymentInformation(
                                    current.getDeploymentInformation()
                            )
                            .podInformation(
                                    current.getPodInformation()
                            )
                            .podLogs(toolResult)
                            .events(
                                    current.getEvents()
                            )
                            .metrics(
                                    current.getMetrics()
                            )
                            .build();

            case "kubernetes_events" ->
                    AgentEvidenceContext.builder()
                            .deploymentInformation(
                                    current.getDeploymentInformation()
                            )
                            .podInformation(
                                    current.getPodInformation()
                            )
                            .podLogs(
                                    current.getPodLogs()
                            )
                            .events(toolResult)
                            .metrics(
                                    current.getMetrics()
                            )
                            .build();

            case "kubernetes_metrics" ->
                    AgentEvidenceContext.builder()
                            .deploymentInformation(
                                    current.getDeploymentInformation()
                            )
                            .podInformation(
                                    current.getPodInformation()
                            )
                            .podLogs(
                                    current.getPodLogs()
                            )
                            .events(
                                    current.getEvents()
                            )
                            .metrics(toolResult)
                            .build();

            default ->
                    current;
        };
    }
}