package com.kubemanager.ai_service.agent.tool.deployment;

import com.kubemanager.ai_service.agent.AiTool;
import com.kubemanager.ai_service.agent.tool.ToolDefinition;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import com.kubemanager.ai_service.client.DeploymentServiceClient;
import com.kubemanager.ai_service.dto.DeploymentCreateRequest;
import com.kubemanager.ai_service.dto.DeploymentCreateServiceApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeploymentCreateTool implements AiTool {

    private static final String TOOL_NAME =
            "deployment_create";

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
                        "Creates a Kubernetes deployment in a specified cluster and namespace. " +
                                "Use this when the user explicitly asks to create or deploy a new application."
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
                              "description": "Kubernetes namespace where the deployment will be created"
                            },
                            "name": {
                              "type": "string",
                              "description": "Name of the deployment"
                            },
                            "image": {
                              "type": "string",
                              "description": "Container image to deploy"
                            },
                            "replicas": {
                              "type": "integer",
                              "description": "Number of desired replicas"
                            }
                          },
                          "required": [
                            "clusterId",
                            "namespace",
                            "name",
                            "image"
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

        Object nameValue =
                arguments.get("name");

        Object imageValue =
                arguments.get("image");

        Object replicasValue =
                arguments.get("replicas");

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

        if (nameValue == null
                || nameValue.toString().isBlank()) {

            return failure(
                    "Deployment name is required."
            );
        }

        if (imageValue == null
                || imageValue.toString().isBlank()) {

            return failure(
                    "Container image is required."
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

        String name =
                nameValue.toString().trim();

        String image =
                imageValue.toString().trim();

        Integer replicas = null;

        if (replicasValue != null) {

            try {

                replicas = Integer.valueOf(
                        replicasValue.toString()
                );

                if (replicas < 1) {

                    return failure(
                            "replicas must be greater than zero."
                    );
                }

            } catch (NumberFormatException exception) {

                return failure(
                        "Invalid replicas. Expected an integer."
                );
            }
        }

        try {

            log.info(
                    "Executing '{}' for clusterId={}, namespace={}, name={}, image={}, replicas={}",
                    TOOL_NAME,
                    clusterId,
                    namespace,
                    name,
                    image,
                    replicas
            );

            DeploymentCreateRequest createRequest =
                    DeploymentCreateRequest.builder()
                            .name(name)
                            .namespace(namespace)
                            .image(image)
                            .replicas(replicas)
                            .build();

            DeploymentCreateServiceApiResponse response =
                    deploymentServiceClient.createDeployment(
                            clusterId,
                            createRequest
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
                    "Failed to execute '{}' for clusterId={}, namespace={}, name={}",
                    TOOL_NAME,
                    clusterId,
                    namespace,
                    name,
                    exception
            );

            return failure(
                    "Failed to create deployment."
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