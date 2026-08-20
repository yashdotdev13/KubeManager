package com.kubemanager.ai_service.agent.tool.pod;

import com.kubemanager.ai_service.agent.AiTool;
import com.kubemanager.ai_service.agent.tool.ToolDefinition;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import com.kubemanager.ai_service.client.ClusterServiceClient;
import com.kubemanager.ai_service.dto.PodListServiceApiResponse;
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

    private final ClusterServiceClient clusterServiceClient;

    @Override
    public String getName() {
        return TOOL_NAME;
    }

    @Override
    public ToolDefinition getToolDefinition() {

        return ToolDefinition.builder()
                .name(TOOL_NAME)
                .description(
                        "Lists Kubernetes pods in a cluster. " +
                                "Can optionally filter pods by namespace. " +
                                "If namespace is not provided, pods from all namespaces are returned."
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
                              "description": "Optional Kubernetes namespace. If omitted, pods from all namespaces are returned."
                            }
                          },
                          "required": ["clusterId"]
                        }
                        """)
                .build();
    }

    @Override
    public ToolResponse execute(ToolRequest request) {

        if (request == null) {
            return failure("Tool request cannot be null.");
        }

        Map<String, Object> arguments = request.getArguments();

        if (arguments == null || arguments.isEmpty()) {
            return failure("Tool arguments are required.");
        }

        Object clusterIdValue = arguments.get("clusterId");

        if (clusterIdValue == null
                || clusterIdValue.toString().isBlank()) {

            return failure("clusterId is required.");
        }

        UUID clusterId;

        try {

            clusterId = UUID.fromString(
                    clusterIdValue.toString()
            );

        } catch (IllegalArgumentException ex) {

            log.warn(
                    "Invalid clusterId received by '{}': {}",
                    TOOL_NAME,
                    clusterIdValue
            );

            return failure(
                    "Invalid clusterId. Expected a valid UUID."
            );
        }

        Object namespaceValue = arguments.get("namespace");

        String namespace = null;

        if (namespaceValue != null
                && !namespaceValue.toString().isBlank()) {

            namespace = namespaceValue.toString().trim();
        }

        try {

            log.info(
                    "Executing '{}' for clusterId={}, namespace={}",
                    TOOL_NAME,
                    clusterId,
                    namespace
            );

            PodListServiceApiResponse response =
                    clusterServiceClient.getPods(
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

        } catch (Exception ex) {

            log.error(
                    "Failed to execute '{}' for clusterId={}, namespace={}",
                    TOOL_NAME,
                    clusterId,
                    namespace,
                    ex
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