package com.kubemanager.ai_service.agent.tool.pod;

import com.kubemanager.ai_service.agent.AiTool;
import com.kubemanager.ai_service.agent.tool.ToolDefinition;
import com.kubemanager.ai_service.agent.tool.ToolRequest;
import com.kubemanager.ai_service.agent.tool.ToolResponse;
import com.kubemanager.ai_service.client.ClusterServiceClient;
import com.kubemanager.ai_service.dto.PodInfoServiceApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PodInfoTool implements AiTool {

    private static final String TOOL_NAME = "pod_info";

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
                        "Retrieves detailed information about a specific Kubernetes pod, " +
                                "including its status, node, IP addresses, QoS class, service account, " +
                                "start time, labels and annotations."
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
                              "description": "Kubernetes namespace containing the pod"
                            },
                            "podName": {
                              "type": "string",
                              "description": "Name of the Kubernetes pod"
                            }
                          },
                          "required": ["clusterId", "namespace", "podName"]
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
        Object namespaceValue = arguments.get("namespace");
        Object podNameValue = arguments.get("podName");

        if (clusterIdValue == null
                || clusterIdValue.toString().isBlank()) {

            return failure("clusterId is required.");
        }

        if (namespaceValue == null
                || namespaceValue.toString().isBlank()) {

            return failure("namespace is required.");
        }

        if (podNameValue == null
                || podNameValue.toString().isBlank()) {

            return failure("podName is required.");
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

        String podName =
                podNameValue.toString().trim();

        try {

            log.info(
                    "Executing '{}' for clusterId={}, namespace={}, podName={}",
                    TOOL_NAME,
                    clusterId,
                    namespace,
                    podName
            );

            PodInfoServiceApiResponse response =
                    clusterServiceClient.getPod(
                            clusterId,
                            namespace,
                            podName
                    );

            return ToolResponse.builder()
                    .success(true)
                    .message(
                            "Pod information retrieved successfully."
                    )
                    .data(response.getData())
                    .build();

        } catch (Exception ex) {

            log.error(
                    "Failed to execute '{}' for clusterId={}, namespace={}, podName={}",
                    TOOL_NAME,
                    clusterId,
                    namespace,
                    podName,
                    ex
            );

            return failure(
                    "Failed to retrieve pod information."
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