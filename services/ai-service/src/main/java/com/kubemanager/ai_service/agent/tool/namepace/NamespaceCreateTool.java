package com.kubemanager.ai_service.agent.tool.namepace;

import com.kubemanager.ai_service.agent.AiTool;
import com.kubemanager.ai_service.agent.tool.ToolDefinition;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import com.kubemanager.ai_service.client.ClusterServiceClient;
import com.kubemanager.ai_service.dto.NamespaceApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NamespaceCreateTool implements AiTool {

    private static final String TOOL_NAME = "namespace_create";

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
                        "Creates a new Kubernetes namespace in a cluster. " +
                                "Use this tool when the user explicitly asks to create " +
                                "or add a Kubernetes namespace."
                )
                .inputSchema("""
                        {
                          "type": "object",
                          "properties": {
                            "clusterId": {
                              "type": "string",
                              "description": "UUID of the Kubernetes cluster"
                            },
                            "name": {
                              "type": "string",
                              "description": "Name of the Kubernetes namespace to create"
                            }
                          },
                          "required": ["clusterId", "name"]
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

        Object nameValue =
                arguments.get("name");

        if (clusterIdValue == null
                || clusterIdValue.toString().isBlank()) {

            return failure("clusterId is required.");
        }

        if (nameValue == null
                || nameValue.toString().isBlank()) {

            return failure("Namespace name is required.");
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

        String name =
                nameValue.toString().trim();

        try {

            log.info(
                    "Executing '{}' for clusterId={}, namespace={}",
                    TOOL_NAME,
                    clusterId,
                    name
            );

            NamespaceApiResponse response =
                    clusterServiceClient.createNamespace(
                            clusterId,
                            name
                    );

            return ToolResponse.builder()
                    .success(true)
                    .message(
                            "Namespace created successfully."
                    )
                    .data(response)
                    .build();

        } catch (Exception ex) {

            log.error(
                    "Failed to execute '{}' for clusterId={}, namespace={}",
                    TOOL_NAME,
                    clusterId,
                    name,
                    ex
            );

            return failure(
                    "Failed to create namespace."
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