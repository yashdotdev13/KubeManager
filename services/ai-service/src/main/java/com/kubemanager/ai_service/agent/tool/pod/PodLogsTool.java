package com.kubemanager.ai_service.agent.tool.pod;

import com.kubemanager.ai_service.agent.AiTool;
import com.kubemanager.ai_service.agent.tool.ToolDefinition;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import com.kubemanager.ai_service.client.PodLogsServiceClient;
import com.kubemanager.ai_service.dto.PodLogsServiceApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PodLogsTool implements AiTool {

    private static final String TOOL_NAME = "pod_logs";

    private final PodLogsServiceClient podLogsServiceClient;

    @Override
    public String getName() {

        return TOOL_NAME;
    }

    @Override
    public ToolDefinition getToolDefinition() {

        return ToolDefinition.builder()
                .name(TOOL_NAME)
                .description(
                        "Retrieves logs from a specific Kubernetes pod. " +
                                "Supports selecting a specific container, limiting " +
                                "the number of returned log lines, and retrieving " +
                                "logs from a previously terminated container."
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
                              "description": "Kubernetes namespace containing the pod"
                            },
                            "podName": {
                              "type": "string",
                              "description": "Name of the Kubernetes pod"
                            },
                            "container": {
                              "type": "string",
                              "description": "Optional name of the container. If omitted, the first container is used."
                            },
                            "tailLines": {
                              "type": "integer",
                              "description": "Optional number of recent log lines to retrieve"
                            },
                            "previous": {
                              "type": "boolean",
                              "description": "Whether to retrieve logs from the previously terminated container"
                            }
                          },
                          "required": [
                            "clusterId",
                            "namespace",
                            "podName"
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

        Object namespaceValue =
                arguments.get("namespace");

        Object podNameValue =
                arguments.get("podName");

        if (clusterIdValue == null
                || clusterIdValue.toString().isBlank()) {

            return failure(
                    "clusterId is required."
            );
        }

        if (namespaceValue == null
                || namespaceValue.toString().isBlank()) {

            return failure(
                    "namespace is required."
            );
        }

        if (podNameValue == null
                || podNameValue.toString().isBlank()) {

            return failure(
                    "podName is required."
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

        String namespace =
                namespaceValue
                        .toString()
                        .trim();

        String podName =
                podNameValue
                        .toString()
                        .trim();

        String container =
                null;

        Object containerValue =
                arguments.get("container");

        if (containerValue != null
                && !containerValue
                .toString()
                .isBlank()) {

            container =
                    containerValue
                            .toString()
                            .trim();
        }

        Integer tailLines =
                null;

        Object tailLinesValue =
                arguments.get("tailLines");

        if (tailLinesValue != null) {

            try {

                if (tailLinesValue instanceof Number number) {

                    tailLines =
                            number.intValue();

                } else {

                    tailLines =
                            Integer.parseInt(
                                    tailLinesValue
                                            .toString()
                                            .trim()
                            );
                }

            } catch (NumberFormatException exception) {

                return failure(
                        "Invalid tailLines. Expected an integer."
                );
            }

            if (tailLines <= 0) {

                return failure(
                        "tailLines must be greater than zero."
                );
            }
        }

        Boolean previous =
                null;

        Object previousValue =
                arguments.get("previous");

        if (previousValue != null) {

            if (previousValue instanceof Boolean booleanValue) {

                previous =
                        booleanValue;

            } else {

                String previousString =
                        previousValue
                                .toString()
                                .trim();

                if (!previousString.equalsIgnoreCase("true")
                        && !previousString.equalsIgnoreCase("false")) {

                    return failure(
                            "Invalid previous value. " +
                                    "Expected true or false."
                    );
                }

                previous =
                        Boolean.parseBoolean(
                                previousString
                        );
            }
        }

        try {

            log.info(
                    "Executing '{}' for clusterId={}, " +
                            "namespace={}, podName={}, container={}, " +
                            "tailLines={}, previous={}",
                    TOOL_NAME,
                    clusterId,
                    namespace,
                    podName,
                    container,
                    tailLines,
                    previous
            );

            PodLogsServiceApiResponse response =
                    podLogsServiceClient.getPodLogs(
                            clusterId,
                            namespace,
                            podName,
                            container,
                            tailLines,
                            previous
                    );

            if (response == null) {

                return failure(
                        "Pod logs service returned no response."
                );
            }

            /*
             * -----------------------------------------------------
             * SERVICE RESPONSE VALIDATION
             * -----------------------------------------------------
             */

            if (!response.isSuccess()) {

                return failure(
                        response.getMessage() != null
                                ? response.getMessage()
                                : "Failed to retrieve pod logs."
                );
            }

            if (response.getData() == null) {

                return failure(
                        "Pod logs service returned empty data."
                );
            }
            return ToolResponse.builder()
                    .success(true)
                    .message(
                            "Pod logs retrieved successfully."
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
                    "Failed to retrieve pod logs."
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