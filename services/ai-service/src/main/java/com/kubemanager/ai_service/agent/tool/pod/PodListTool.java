package com.kubemanager.ai_service.agent.tool.pod;

import com.kubemanager.ai_service.agent.AiTool;
import com.kubemanager.ai_service.agent.tool.ToolDefinition;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import com.kubemanager.ai_service.client.PodServiceClient;
import com.kubemanager.ai_service.dto.PodServiceApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PodListTool implements AiTool {

    private static final String TOOL_NAME = "pod_list";

    private final PodServiceClient podServiceClient;

    @Override
    public String getName() {
        return TOOL_NAME;
    }

    @Override
    public ToolDefinition getToolDefinition() {

        return ToolDefinition.builder()
                .name(TOOL_NAME)
                .description(
                        "Lists Kubernetes pods in a specific cluster and namespace. " +
                                "Returns pod name, namespace, status, node, pod IP and restart count."
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
                              "description": "Kubernetes namespace whose pods should be listed"
                            }
                          },
                          "required": ["clusterId", "namespace"]
                        }
                        """)
                .build();
    }

    @Override
    public ToolResponse execute(ToolRequest request) {

        if (request == null) {
            return failure(
                    "Tool request cannot be null."
            );
        }

        Map<String, Object> arguments =
                request.getArguments();

        if (arguments == null || arguments.isEmpty()) {
            return failure(
                    "Tool arguments are required."
            );
        }

        Object clusterIdValue =
                arguments.get("clusterId");

        Object namespaceValue =
                arguments.get("namespace");

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

        UUID clusterId;

        try {

            clusterId = UUID.fromString(
                    clusterIdValue.toString()
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
                namespaceValue.toString().trim();

        try {

            log.info(
                    "Executing '{}' for clusterId={}, namespace={}",
                    TOOL_NAME,
                    clusterId,
                    namespace
            );

            PodServiceApiResponse response =
                    podServiceClient.getPods(
                            clusterId,
                            namespace
                    );

            return ToolResponse.builder()
                    .success(true)
                    .message(
                            "Pods retrieved successfully."
                    )
                    .data(response.getData())
                    .build();

        } catch (Exception exception) {

            log.error(
                    "Failed to execute '{}' for clusterId={}, namespace={}",
                    TOOL_NAME,
                    clusterId,
                    namespace,
                    exception
            );

            return failure(
                    "Failed to retrieve pods."
            );
        }
    }

    private ToolResponse failure(String message) {

        return ToolResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}