package com.kubemanager.cluster_service.kubernates.service;

import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.kubernates.metadata.ClusterMetadata;
import com.kubemanager.exception.BadRequestException;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.VersionInfo;
import io.fabric8.kubernetes.client.dsl.MixedOperation;


import com.kubemanager.cluster_service.dto.response.NodeResponse;
import io.fabric8.kubernetes.api.model.NodeAddress;
import io.fabric8.kubernetes.api.model.NodeCondition;
import io.fabric8.kubernetes.api.model.NodeSystemInfo;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.client.dsl.MixedOperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KubernetesConnectionServiceImplTest {

    @Mock
    private KubernetesClientFactory clientFactory;

    @Mock
    private KubernetesClient client;

    @InjectMocks
    private KubernetesConnectionServiceImpl service;

    @Test
    void connect_shouldRejectNullFile() {
        assertThatThrownBy(() ->
                service.connect((MultipartFile) null)
        ).isInstanceOf(BadRequestException.class);

        verify(clientFactory).createClient((MultipartFile) null);
    }

    @Test
    void connect_shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "kubeConfig",
                "config.yaml",
                "application/yaml",
                new byte[0]
        );

        assertThatThrownBy(() -> service.connect(file))
                .isInstanceOf(BadRequestException.class);

        verify(clientFactory).createClient(file);
    }

    @Test
    void connect_shouldRejectBlankConfigContent() {
        assertThatThrownBy(() -> service.connect("   "))
                .isInstanceOf(BadRequestException.class);

        verify(clientFactory).createClient("   ");
    }

    @Test
    void connect_shouldTranslateFactoryFailure() {
        MockMultipartFile file = new MockMultipartFile(
                "kubeConfig",
                "config.yaml",
                "application/yaml",
                "invalid-config".getBytes()
        );

        when(clientFactory.createClient(file))
                .thenThrow(new RuntimeException("Invalid kubeconfig"));

        assertThatThrownBy(() -> service.connect(file))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getNodes_shouldTranslateFactoryFailure() {
        when(clientFactory.createClient("valid-config"))
                .thenThrow(new RuntimeException("Unable to create client"));

        assertThatThrownBy(() -> service.getNodes("valid-config"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void fetchClusterMetadata_shouldReturnClusterInformation()
            throws MalformedURLException {

        // Arrange: Kubernetes version
        VersionInfo versionInfo = mock(VersionInfo.class);

        when(client.getKubernetesVersion()).thenReturn(versionInfo);
        when(versionInfo.getGitVersion()).thenReturn("v1.30.0");

        // Arrange: API server URL
        Config config = mock(Config.class);

        URL apiServerUrl =
                URI.create("https://localhost:6443").toURL();

        when(client.getConfiguration()).thenReturn(config);
        when(config.getMasterUrl()).thenReturn(String.valueOf(apiServerUrl));

        // Arrange: Kubernetes nodes
        @SuppressWarnings("unchecked")
        MixedOperation<Node, NodeList,
                io.fabric8.kubernetes.client.dsl.Resource<Node>>
                nodes = mock(MixedOperation.class);

        when(client.nodes()).thenReturn(nodes);

        NodeList nodeList = new NodeList();
        nodeList.setItems(List.of(
                new Node(),
                new Node()
        ));

        when(nodes.list()).thenReturn(nodeList);

        // Arrange: Kubernetes namespaces
        @SuppressWarnings("unchecked")
        MixedOperation<Namespace, NamespaceList,
                io.fabric8.kubernetes.client.dsl.Resource<Namespace>>
                namespaces = mock(MixedOperation.class);

        when(client.namespaces()).thenReturn(namespaces);

        NamespaceList namespaceList = new NamespaceList();
        namespaceList.setItems(List.of(new Namespace()));

        when(namespaces.list()).thenReturn(namespaceList);

        // Act
        ClusterMetadata metadata = service.fetchClusterMetadata(client);

        // Assert
        assertThat(metadata).isNotNull();
        assertThat(metadata.getApiServer())
                .isEqualTo("https://localhost:6443");
        assertThat(metadata.getKubernetesVersion())
                .isEqualTo("v1.30.0");
        assertThat(metadata.getNodeCount()).isEqualTo(2);
        assertThat(metadata.getNamespaceCount()).isEqualTo(1);

        verify(client).getKubernetesVersion();
        verify(client).getConfiguration();
        verify(client).nodes();
        verify(client).namespaces();
    }

    @Test
    void getNodes_shouldMapNodeInformationCorrectly() {
        Node node = createNode(
                "worker-1",
                "True",
                Map.of("node-role.kubernetes.io/worker", ""),
                "10.0.0.10",
                "192.168.1.10"
        );

        List<NodeResponse> responses = executeGetNodes(List.of(node));

        assertThat(responses).hasSize(1);

        NodeResponse response = responses.get(0);

        assertThat(response.getName()).isEqualTo("worker-1");
        assertThat(response.getStatus()).isEqualTo("READY");
        assertThat(response.getRole()).isEqualTo("WORKER");
        assertThat(response.getKubernetesVersion()).isEqualTo("v1.30.0");
        assertThat(response.getOperatingSystem()).isEqualTo("linux");
        assertThat(response.getArchitecture()).isEqualTo("amd64");
        assertThat(response.getKernelVersion()).isEqualTo("6.1.0");
        assertThat(response.getContainerRuntime()).isEqualTo("containerd://1.7.0");
        assertThat(response.getOsImage()).isEqualTo("Linux");
        assertThat(response.getInternalIp()).isEqualTo("10.0.0.10");
        assertThat(response.getExternalIp()).isEqualTo("192.168.1.10");
        assertThat(response.getLabels())
                .containsEntry("node-role.kubernetes.io/worker", "");
        assertThat(response.getCapacity())
                .containsEntry("cpu", "4")
                .containsEntry("memory", "16");

        assertThat(response.getAllocatable())
                .containsEntry("cpu", "3")
                .containsEntry("memory", "12");
    }

    @Test
    void getNodes_shouldIdentifyControlPlaneNode() {
        Node node = createNode(
                "control-plane-1",
                "True",
                Map.of("node-role.kubernetes.io/control-plane", ""),
                "10.0.0.1",
                null
        );

        List<NodeResponse> responses = executeGetNodes(List.of(node));

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getRole()).isEqualTo("CONTROL_PLANE");
        assertThat(responses.get(0).getStatus()).isEqualTo("READY");
        assertThat(responses.get(0).getExternalIp()).isNull();
    }

    @Test
    void getNodes_shouldMarkNodeNotReady() {
        Node node = createNode(
                "worker-2",
                "False",
                Map.of("node-role.kubernetes.io/worker", ""),
                "10.0.0.20",
                null
        );

        List<NodeResponse> responses = executeGetNodes(List.of(node));

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo("NOT_READY");
    }

    @Test
    void getNodes_shouldReturnUnknownWhenReadyConditionIsMissing() {
        Node node = createNode(
                "worker-3",
                null,
                Map.of("node-role.kubernetes.io/worker", ""),
                null,
                null
        );

        List<NodeResponse> responses = executeGetNodes(List.of(node));

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getStatus()).isEqualTo("UNKNOWN");
    }

    @Test
    void getNodes_shouldReturnWorkerWhenRoleLabelsAreMissing() {
        Node node = createNode(
                "worker-4",
                "True",
                Map.of("environment", "testing"),
                null,
                null
        );

        List<NodeResponse> responses = executeGetNodes(List.of(node));

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getRole()).isEqualTo("WORKER");
    }

    @Test
    void getNodes_shouldReturnEmptyListWhenClusterHasNoNodes() {
        List<NodeResponse> responses = executeGetNodes(List.of());

        assertThat(responses).isEmpty();
    }

    @Test
    void getNodes_shouldReturnMasterLabelAsControlPlane() {
        Node node = createNode(
                "master-1",
                "True",
                Map.of("node-role.kubernetes.io/master", ""),
                null,
                null
        );

        List<NodeResponse> responses = executeGetNodes(List.of(node));

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getRole()).isEqualTo("CONTROL_PLANE");
    }

    /**
     * Executes getNodes(KubernetesClient) with a mocked Fabric8 node list.
     */
    @SuppressWarnings("unchecked")
    private List<NodeResponse> executeGetNodes(List<Node> nodeItems) {
        MixedOperation<Node, NodeList,
                io.fabric8.kubernetes.client.dsl.Resource<Node>> nodes =
                mock(MixedOperation.class);

        NodeList nodeList = new NodeList();
        nodeList.setItems(new ArrayList<>(nodeItems));

        when(client.nodes()).thenReturn(nodes);
        when(nodes.list()).thenReturn(nodeList);

        return service.getNodes(client);
    }

    /**
     * Builds a Fabric8 Node model without starting a Kubernetes cluster.
     */
    private Node createNode(
            String name,
            String readyStatus,
            Map<String, String> labels,
            String internalIp,
            String externalIp
    ) {
        Node node = new Node();

        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(name);
        metadata.setLabels(new HashMap<>(labels));
        node.setMetadata(metadata);

        NodeSystemInfo nodeInfo = new NodeSystemInfo();
        nodeInfo.setKubeletVersion("v1.30.0");
        nodeInfo.setOperatingSystem("linux");
        nodeInfo.setArchitecture("amd64");
        nodeInfo.setKernelVersion("6.1.0");
        nodeInfo.setContainerRuntimeVersion("containerd://1.7.0");
        nodeInfo.setOsImage("Linux");

        io.fabric8.kubernetes.api.model.NodeStatus status =
                new io.fabric8.kubernetes.api.model.NodeStatus();

        status.setNodeInfo(nodeInfo);

        if (readyStatus != null) {
            NodeCondition condition = new NodeCondition();
            condition.setType("Ready");
            condition.setStatus(readyStatus);
            status.setConditions(List.of(condition));
        } else {
            status.setConditions(new ArrayList<>());
        }

        List<NodeAddress> addresses = new ArrayList<>();

        if (internalIp != null) {
            NodeAddress address = new NodeAddress();
            address.setType("InternalIP");
            address.setAddress(internalIp);
            addresses.add(address);
        }

        if (externalIp != null) {
            NodeAddress address = new NodeAddress();
            address.setType("ExternalIP");
            address.setAddress(externalIp);
            addresses.add(address);
        }

        status.setAddresses(addresses);

        Map<String, Quantity> capacity = new HashMap<>();
        capacity.put("cpu", new Quantity("4"));
        capacity.put("memory", new Quantity("16Gi"));
        status.setCapacity(capacity);

        Map<String, Quantity> allocatable = new HashMap<>();
        allocatable.put("cpu", new Quantity("3"));
        allocatable.put("memory", new Quantity("12Gi"));
        status.setAllocatable(allocatable);

        node.setStatus(status);

        return node;
    }
}