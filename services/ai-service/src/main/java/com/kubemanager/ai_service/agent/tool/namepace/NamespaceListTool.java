package com.kubemanager.ai_service.agent.tool.namepace;


import com.kubemanager.ai_service.agent.AiTool;
import com.kubemanager.ai_service.agent.tool.ToolDefinition;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import com.kubemanager.ai_service.client.NamespaceServiceClient;
import com.kubemanager.ai_service.dto.NamespaceServiceApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NamespaceListTool implements AiTool {

    private static final String TOOL_NAME = "namespace_list";

    private final NamespaceServiceClient namespaceServiceClient;

    @Override
    public String getName() {
        return TOOL_NAME;
    }

    @Override
    public ToolDefinition getToolDefinition() {

        return ToolDefinition.builder()
                .name(TOOL_NAME)
                .description(
                        "Lists all Kubernetes namespaces available in a cluster. " +
                                "Use this tool when the user asks to list, show, " +
                                "or get all namespaces of a Kubernetes cluster."
                )
                .inputSchema("""
                        {
                          "type": "object",
                          "properties": {
                            "clusterId": {
                              "type": "string",
                              "description": "UUID of the Kubernetes cluster"
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

        if (clusterIdValue == null ||
                clusterIdValue.toString().isBlank()) {

            return failure("clusterId is required.");
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

        try {

            log.info(
                    "Executing '{}' for clusterId={}",
                    TOOL_NAME,
                    clusterId
            );

            NamespaceServiceApiResponse response =
                    namespaceServiceClient.getNamespaces(
                            clusterId
                    );

            return ToolResponse.builder()
                    .success(true)
                    .message(
                            "Namespaces retrieved successfully."
                    )
                    .data(response)
                    .build();

        } catch (Exception exception) {

            log.error(
                    "Failed to execute '{}' for clusterId={}",
                    TOOL_NAME,
                    clusterId,
                    exception
            );

            return failure(
                    "Failed to retrieve namespaces."
            );
        }
    }

    private ToolResponse failure(String message) {

        return ToolResponse.builder()
                .success(true)
                .message(
                        "Namespaces retrieved successfully."
                )
                .data(message)
                .build();
    }
}