package com.kubemanager.ai_service.agent.tool.cluster;

import com.kubemanager.ai_service.agent.AiTool;
import com.kubemanager.ai_service.agent.tool.ToolDefinition;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import com.kubemanager.ai_service.client.ClusterServiceClient;
import com.kubemanager.ai_service.dto.ClusterHealthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClusterInfoTool implements AiTool {

    private static final String TOOL_NAME = "cluster_info";

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
                        "Retrieves detailed information about a Kubernetes cluster, " +
                                "including its name, status, Kubernetes version, API server, " +
                                "node count, namespace count and timestamps."
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

        Map<String, Object> arguments =
                request.getArguments();

        if (arguments == null || arguments.isEmpty()) {
            return failure("Tool arguments are required.");
        }

        Object clusterIdValue =
                arguments.get("clusterId");

        if (clusterIdValue == null
                || clusterIdValue.toString().isBlank()) {

            return failure("clusterId is required.");
        }

        UUID clusterId;

        try {

            clusterId =
                    UUID.fromString(
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

        try {

            log.info(
                    "Executing '{}' for clusterId={}",
                    TOOL_NAME,
                    clusterId
            );

            ClusterHealthResponse response =
                    clusterServiceClient.healthCheck(
                            clusterId
                    );

            return ToolResponse.builder()
                    .success(true)
                    .message(
                            "Cluster information retrieved successfully."
                    )
                    .data(response)
                    .build();

        } catch (Exception ex) {

            log.error(
                    "Failed to execute '{}' for clusterId={}",
                    TOOL_NAME,
                    clusterId,
                    ex
            );

            return failure(
                    "Failed to retrieve cluster information."
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
