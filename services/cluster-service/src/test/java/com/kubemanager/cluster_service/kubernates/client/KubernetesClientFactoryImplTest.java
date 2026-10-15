package com.kubemanager.cluster_service.kubernates.client;

import com.kubemanager.exception.BadRequestException;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KubernetesClientFactoryImplTest {

    private final KubernetesClientFactoryImpl factory =
            new KubernetesClientFactoryImpl();

    @Test
    void createClient_shouldRejectNullFile() {
        assertThatThrownBy(() -> factory.createClient((MultipartFile) null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createClient_shouldRejectEmptyFile() {
        MultipartFile file = new MockMultipartFile(
                "kubeConfig", "config.yaml", "text/yaml", new byte[0]);

        assertThatThrownBy(() -> factory.createClient(file))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createClient_shouldRejectNullContent() {
        assertThatThrownBy(() -> factory.createClient((String) null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createClient_shouldRejectBlankContent() {
        assertThatThrownBy(() -> factory.createClient("   "))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createClient_shouldTranslateFileReadFailure() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenThrow(new IOException("read failure"));

        assertThatThrownBy(() -> factory.createClient(file))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createClient_shouldBuildClientFromValidConfiguration() {
        String kubeConfig = """
                apiVersion: v1
                kind: Config
                clusters:
                  - name: test-cluster
                    cluster:
                      server: https://127.0.0.1:6443
                contexts:
                  - name: test-context
                    context:
                      cluster: test-cluster
                      user: test-user
                current-context: test-context
                users:
                  - name: test-user
                    user: {}
                """;

        Config config = Config.fromKubeconfig(kubeConfig);

        try (MockedStatic<Config> configMock = mockStatic(Config.class);
             org.mockito.MockedConstruction<KubernetesClientBuilder> construction =
                     mockConstruction(
                             KubernetesClientBuilder.class,
                             (builder, context) -> {
                                 when(builder.withConfig(config)).thenReturn(builder);
                                 when(builder.build()).thenReturn(
                                         mock(KubernetesClient.class));
                             })) {

            configMock.when(() -> Config.fromKubeconfig(kubeConfig))
                    .thenReturn(config);

            KubernetesClient result = factory.createClient(kubeConfig);

            assertThat(result).isNotNull();
            assertThat(construction.constructed()).hasSize(1);

            KubernetesClientBuilder builder =
                    construction.constructed().get(0);

            verify(builder).withConfig(config);
            verify(builder).build();
        }
    }

    @Test
    void createClient_shouldTranslateInvalidConfiguration() {
        String invalidConfig = "this is not a valid kubeconfig";

        try (MockedStatic<Config> configMock = mockStatic(Config.class)) {
            configMock.when(() -> Config.fromKubeconfig(invalidConfig))
                    .thenThrow(new IllegalArgumentException("Invalid config"));

            assertThatThrownBy(() -> factory.createClient(invalidConfig))
                    .isInstanceOf(BadRequestException.class);
        }
    }
}