package com.kubemanager.ai_service.agent.tool.namepace;

import com.kubemanager.ai_service.agent.AiTool;
import com.kubemanager.ai_service.agent.tool.ToolDefinition;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import com.kubemanager.ai_service.client.ClusterServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NamespaceDeleteTool implements AiTool {

    private static final String TOOL_NAME = "namespace_delete";

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
                        "Deletes a Kubernetes namespace from a cluster. " +
                                "This is a destructive operation and should only be used " +
                                "when the user explicitly requests namespace deletion."
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
                              "description": "Name of the Kubernetes namespace to delete"
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
            return failure("Tool request cannot be null.");
        }

        Map<String, Object> arguments =
                request.getArguments();

        if (arguments == null || arguments.isEmpty()) {
            return failure("Tool arguments are required.");
        }

        Object clusterIdValue =
                arguments.get("clusterId");

        Object namespaceValue =
                arguments.get("namespace");

        if (clusterIdValue == null
                || clusterIdValue.toString().isBlank()) {

            return failure("clusterId is required.");
        }

        if (namespaceValue == null
                || namespaceValue.toString().isBlank()) {

            return failure("namespace is required.");
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

        String namespace =
                namespaceValue.toString().trim();

        try {

            log.info(
                    "Executing '{}' for clusterId={}, namespace={}",
                    TOOL_NAME,
                    clusterId,
                    namespace
            );

            clusterServiceClient.deleteNamespace(
                    clusterId,
                    namespace
            );

            return ToolResponse.builder()
                    .success(true)
                    .message(
                            "Namespace '" + namespace +
                                    "' deleted successfully."
                    )
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
                    "Failed to delete namespace."
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