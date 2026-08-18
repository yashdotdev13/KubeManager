package com.kubemanager.ai_service.agent.tool.cluster;


import com.kubemanager.ai_service.agent.AiTool;
import com.kubemanager.ai_service.agent.tool.ToolDefinition;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import com.kubemanager.ai_service.client.ClusterServiceClient;
import com.kubemanager.ai_service.dto.ClusterHealthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClusterHealthTool implements AiTool {

    private static final String TOOL_NAME =
            "cluster_health";

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
                        "Checks the health status of a Kubernetes cluster."
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

        Object clusterIdValue =
                request.getArguments().get("clusterId");

        if (clusterIdValue == null) {
            return ToolResponse.builder()
                    .success(false)
                    .message("clusterId is required.")
                    .build();
        }

        try {

            UUID clusterId =
                    UUID.fromString(clusterIdValue.toString());

            ClusterHealthResponse response =
                    clusterServiceClient.healthCheck(clusterId);

            return ToolResponse.builder()
                    .success(true)
                    .data(response)
                    .message(
                            "Cluster health check completed successfully."
                    )
                    .build();

        } catch (IllegalArgumentException ex) {
            return ToolResponse.builder()
                    .success(false)
                    .message(
                            "Invalid clusterId. Expected a valid UUID."
                    )
                    .build();

        } catch (Exception ex) {
            return ToolResponse.builder()
                    .success(false)
                    .message(
                            "Failed to check cluster health."
                    )
                    .build();
        }
    }
}
