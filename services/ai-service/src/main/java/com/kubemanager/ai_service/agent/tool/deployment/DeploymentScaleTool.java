package com.kubemanager.ai_service.agent.tool.deployment;


import com.kubemanager.ai_service.agent.AiTool;
import com.kubemanager.ai_service.agent.tool.ToolDefinition;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import com.kubemanager.ai_service.client.DeploymentServiceClient;
import com.kubemanager.ai_service.dto.DeploymentScaleServiceApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeploymentScaleTool implements AiTool {

    private static final String TOOL_NAME =
            "deployment_scale";

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
                        "Scales a Kubernetes deployment to the specified number " +
                                "of replicas."
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
                              "description": "Name of the Kubernetes namespace"
                            },
                            "deploymentName": {
                              "type": "string",
                              "description": "Name of the deployment"
                            },
                            "replicas": {
                              "type": "integer",
                              "minimum": 0,
                              "description": "Desired number of deployment replicas"
                            }
                          },
                          "required": [
                            "clusterId",
                            "namespace",
                            "deploymentName",
                            "replicas"
                          ]
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

        Object deploymentNameValue =
                arguments.get("deploymentName");

        Object replicasValue =
                arguments.get("replicas");

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

        if (replicasValue == null) {

            return failure("replicas is required.");
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

        Integer replicas;

        try {

            replicas = Integer.valueOf(
                    replicasValue.toString()
            );

        } catch (NumberFormatException exception) {

            return failure(
                    "Invalid replicas. Expected a valid integer."
            );
        }

        if (replicas < 0) {

            return failure(
                    "replicas cannot be negative."
            );
        }

        try {

            log.info(
                    "Executing '{}' for clusterId={}, namespace={}, deployment={}, replicas={}",
                    TOOL_NAME,
                    clusterId,
                    namespace,
                    deploymentName,
                    replicas
            );

            DeploymentScaleServiceApiResponse response =
                    deploymentServiceClient.scaleDeployment(
                            clusterId,
                            namespace,
                            deploymentName,
                            replicas
                    );

            return ToolResponse.builder()
                    .success(true)
                    .message(
                            "Deployment scaled successfully."
                    )
                    .data(response.getData())
                    .build();

        } catch (Exception exception) {

            log.error(
                    "Failed to execute '{}' for clusterId={}, namespace={}, deployment={}, replicas={}",
                    TOOL_NAME,
                    clusterId,
                    namespace,
                    deploymentName,
                    replicas,
                    exception
            );

            return failure(
                    "Failed to scale deployment."
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