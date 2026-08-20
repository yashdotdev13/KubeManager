package com.kubemanager.ai_service.agent.tool.deployment;

import com.kubemanager.ai_service.agent.AiTool;
import com.kubemanager.ai_service.agent.tool.ToolDefinition;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import com.kubemanager.ai_service.client.DeploymentServiceClient;
import com.kubemanager.ai_service.dto.DeploymentServiceApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeploymentDeleteTool implements AiTool {

    private static final String TOOL_NAME = "deployment_delete";

    private final DeploymentServiceClient deploymentServiceClient;

    @Override
    public String getName() {
        return TOOL_NAME;
    }

    @Override
    public ToolDefinition getToolDefinition() {

        return ToolDefinition.builder()
                .name(TOOL_NAME)
                .description(
                        "Deletes a Kubernetes deployment from a specific namespace. " +
                                "Use this when the user explicitly wants to remove a deployment."
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
                              "description": "Kubernetes namespace containing the deployment"
                            },
                            "deploymentName": {
                              "type": "string",
                              "description": "Name of the deployment to delete"
                            }
                          },
                          "required": [
                            "clusterId",
                            "namespace",
                            "deploymentName"
                          ]
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

        Object deploymentNameValue =
                arguments.get("deploymentName");

        if (clusterIdValue == null
                || clusterIdValue.toString().isBlank()) {

            return failure("clusterId is required.");
        }

        if (namespaceValue == null
                || namespaceValue.toString().isBlank()) {

            return failure("namespace is required.");
        }

        if (deploymentNameValue == null
                || deploymentNameValue.toString().isBlank()) {

            return failure("deploymentName is required.");
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

        String deploymentName =
                deploymentNameValue.toString().trim();

        try {

            log.info(
                    "Executing '{}' for clusterId={}, namespace={}, deploymentName={}",
                    TOOL_NAME,
                    clusterId,
                    namespace,
                    deploymentName
            );

            DeploymentServiceApiResponse response =
                    deploymentServiceClient.deleteDeployment(
                            clusterId,
                            namespace,
                            deploymentName
                    );

            return ToolResponse.builder()
                    .success(true)
                    .message(
                            response.getMessage()
                    )
                    .data(
                            response.getData()
                    )
                    .build();

        } catch (Exception exception) {

            log.error(
                    "Failed to execute '{}' for clusterId={}, namespace={}, deploymentName={}",
                    TOOL_NAME,
                    clusterId,
                    namespace,
                    deploymentName,
                    exception
            );

            return failure(
                    "Failed to delete deployment."
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