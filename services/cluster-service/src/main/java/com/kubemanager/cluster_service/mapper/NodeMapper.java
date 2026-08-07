package com.kubemanager.cluster_service.mapper;



import com.kubemanager.cluster_service.dto.response.NodeResponse;
import com.kubemanager.cluster_service.dto.response.NodeSummaryResponse;
import io.fabric8.kubernetes.api.model.Node;
import org.springframework.stereotype.Component;

@Component
public class NodeMapper {

    public NodeSummaryResponse toSummaryResponse(
            Node node
    ) {

        return NodeSummaryResponse.builder()
                .name(
                        node.getMetadata().getName()
                )
                .status(
                        getReadyStatus(node)
                )
                .role(
                        getRole(node)
                )
                .kubernetesVersion(
                        node.getStatus()
                                .getNodeInfo()
                                .getKubeletVersion()
                )
                .internalIp(
                        getInternalIp(node)
                )
                .operatingSystem(
                        node.getStatus()
                                .getNodeInfo()
                                .getOperatingSystem()
                )
                .architecture(
                        node.getStatus()
                                .getNodeInfo()
                                .getArchitecture()
                )
                .build();
    }


    public NodeResponse toResponse(
            Node node
    ) {

        return NodeResponse.builder()
                .name(
                        node.getMetadata().getName()
                )
                .status(
                        getReadyStatus(node)
                )
                .role(
                        getRole(node)
                )
                .kubernetesVersion(
                        node.getStatus()
                                .getNodeInfo()
                                .getKubeletVersion()
                )
                .operatingSystem(
                        node.getStatus()
                                .getNodeInfo()
                                .getOperatingSystem()
                )
                .architecture(
                        node.getStatus()
                                .getNodeInfo()
                                .getArchitecture()
                )
                .kernelVersion(
                        node.getStatus()
                                .getNodeInfo()
                                .getKernelVersion()
                )
                .containerRuntime(
                        node.getStatus()
                                .getNodeInfo()
                                .getContainerRuntimeVersion()
                )
                .osImage(
                        node.getStatus()
                                .getNodeInfo()
                                .getOsImage()
                )
                .internalIp(
                        getInternalIp(node)
                )
                .externalIp(
                        getExternalIp(node)
                )
                .labels(
                        node.getMetadata().getLabels()
                )
                .capacity(
                        node.getStatus()
                                .getCapacity()
                                .entrySet()
                                .stream()
                                .collect(java.util.stream.Collectors.toMap(
                                        java.util.Map.Entry::getKey,
                                        entry -> entry.getValue().getAmount()
                                ))
                )
                .allocatable(
                        node.getStatus()
                                .getAllocatable()
                                .entrySet()
                                .stream()
                                .collect(java.util.stream.Collectors.toMap(
                                        java.util.Map.Entry::getKey,
                                        entry -> entry.getValue().getAmount()
                                ))
                )
                .build();
    }


    private String getReadyStatus(Node node) {

        return node.getStatus()
                .getConditions()
                .stream()
                .filter(condition ->
                        "Ready".equals(condition.getType()))
                .findFirst()
                .map(condition -> condition.getStatus().equals("True")
                        ? "Ready"
                        : "Not Ready")
                .orElse("Unknown");
    }

    private String getRole(Node node) {

        return node.getMetadata()
                .getLabels()
                .keySet()
                .stream()
                .filter(label ->
                        label.startsWith("node-role.kubernetes.io/"))
                .findFirst()
                .map(label ->
                        label.replace("node-role.kubernetes.io/", ""))
                .orElse("worker");
    }


    private String getInternalIp(Node node) {

        return node.getStatus()
                .getAddresses()
                .stream()
                .filter(address ->
                        "InternalIP".equals(address.getType()))
                .findFirst()
                .map(address ->
                        address.getAddress())
                .orElse("N/A");
    }

    private String getExternalIp(Node node) {

        return node.getStatus()
                .getAddresses()
                .stream()
                .filter(address ->
                        "ExternalIP".equals(address.getType()))
                .findFirst()
                .map(address ->
                        address.getAddress())
                .orElse("N/A");
    }
}