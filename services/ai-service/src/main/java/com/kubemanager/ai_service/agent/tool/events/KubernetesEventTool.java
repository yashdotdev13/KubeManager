package com.kubemanager.ai_service.agent.tool.events;

import com.kubemanager.ai_service.agent.AiTool;
import com.kubemanager.ai_service.agent.tool.ToolDefinition;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import com.kubemanager.ai_service.client.KubernetesEventServiceClient;
import com.kubemanager.ai_service.dto.KubernetesEventServiceApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KubernetesEventTool implements AiTool {

    private static final String TOOL_NAME = "kubernetes_events";

    private final KubernetesEventServiceClient
            kubernetesEventServiceClient;

    @Override
    public String getName() {

        return TOOL_NAME;
    }

    @Override
    public ToolDefinition getToolDefinition() {

        return ToolDefinition.builder()
                .name(TOOL_NAME)
                .description(
                        "Retrieves Kubernetes events from a namespace " +
                                "or for a specific Kubernetes pod. Use this tool " +
                                "when investigating scheduling failures, image " +
                                "pull errors, container crashes, mount failures, " +
                                "health-check failures, warnings, or other " +
                                "Kubernetes lifecycle events."
                )
                .inputSchema("""
                        {
                          "type": "object",
                          "properties": {
                            "clusterId": {
                              "type": "string",
                              "description": "UUID of the Kubernetes cluster"
                            },
                            "namespace": {
                              "type": "string",
                              "description": "Kubernetes namespace"
                            },
                            "podName": {
                              "type": "string",
                              "description": "Optional Kubernetes pod name. If provided, retrieve events for that pod."
                            }
                          },
                          "required": [
                            "clusterId",
                            "namespace"
                          ]
                        }
                        """)
                .build();
    }

    @Override
    public ToolResponse execute(
            ToolRequest request
    ) {

        if (request == null) {

            return failure(
                    "Tool request cannot be null."
            );
        }

        Map<String, Object> arguments =
                request.getArguments();

        if (arguments == null
                || arguments.isEmpty()) {

            return failure(
                    "Tool arguments are required."
            );
        }

        Object clusterIdValue =
                arguments.get("clusterId");

        if (clusterIdValue == null
                || clusterIdValue.toString().isBlank()) {

            return failure(
                    "clusterId is required."
            );
        }

        UUID clusterId;

        try {

            clusterId =
                    UUID.fromString(
                            clusterIdValue
                                    .toString()
                                    .trim()
                    );

        } catch (IllegalArgumentException exception) {

            log.warn(
                    "Invalid clusterId received by '{}': {}",
                    TOOL_NAME,
                    clusterIdValue
            );

            return failure(
                    "Invalid clusterId. Expected a valid UUID."
            );
        }

        Object namespaceValue =
                arguments.get("namespace");

        if (namespaceValue == null
                || namespaceValue.toString().isBlank()) {

            return failure(
                    "namespace is required."
            );
        }

        String namespace =
                namespaceValue
                        .toString()
                        .trim();

        String podName = null;

        Object podNameValue =
                arguments.get("podName");

        if (podNameValue != null
                && !podNameValue
                .toString()
                .isBlank()) {

            podName =
                    podNameValue
                            .toString()
                            .trim();
        }

        try {

            log.info(
                    "Executing '{}' for clusterId={}, " +
                            "namespace={}, podName={}",
                    TOOL_NAME,
                    clusterId,
                    namespace,
                    podName
            );

            KubernetesEventServiceApiResponse response;

            /*
             * If podName is provided, retrieve events specifically
             * for that pod.
             */
            if (podName != null) {

                response =
                        kubernetesEventServiceClient
                                .getPodEvents(
                                        clusterId,
                                        namespace,
                                        podName
                                );

            } else {

                /*
                 * Otherwise retrieve namespace events.
                 */
                response =
                        kubernetesEventServiceClient
                                .getEvents(
                                        clusterId,
                                        namespace
                                );
            }


            if (response == null) {

                return failure(
                        "Kubernetes event service returned no response."
                );
            }

            if (!response.isSuccess()) {

                return failure(
                        response.getMessage() != null
                                ? response.getMessage()
                                : "Failed to retrieve Kubernetes events."
                );
            }
            return ToolResponse.builder()
                    .success(true)
                    .message(
                            podName != null
                                    ? "Pod events retrieved successfully."
                                    : "Kubernetes events retrieved successfully."
                    )
                    .data(
                            response.getData()
                    )
                    .build();

        } catch (Exception exception) {

            log.error(
                    "Failed to execute '{}' for clusterId={}, " +
                            "namespace={}, podName={}",
                    TOOL_NAME,
                    clusterId,
                    namespace,
                    podName,
                    exception
            );

            return failure(
                    "Failed to retrieve Kubernetes events."
            );
        }
    }

    private ToolResponse failure(
            String message
    ) {

        return ToolResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}