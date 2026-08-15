package com.kubemanager.cluster_service.service.Impl;

import com.kubemanager.cluster_service.dto.request.PodExecRequest;
import com.kubemanager.cluster_service.dto.response.PodExecResponse;
import com.kubemanager.cluster_service.entity.Cluster;
import com.kubemanager.cluster_service.kubernates.client.KubernetesClientFactory;
import com.kubemanager.cluster_service.repository.ClusterRepository;
import com.kubemanager.cluster_service.service.PodExecService;
import com.kubemanager.exception.BadRequestException;
import com.kubemanager.exception.ErrorCode;
import com.kubemanager.exception.ResourceNotFoundException;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PodExecServiceImpl implements PodExecService {

    private final ClusterRepository clusterRepository;
    private final KubernetesClientFactory kubernetesClientFactory;


    @Override
    public PodExecResponse executeCommand(
            UUID clusterId,
            String namespace,
            String podName,
            PodExecRequest request
    ) {

        log.info(
                "Executing command in pod '{}' in namespace '{}' for cluster '{}'.",
                podName,
                namespace,
                clusterId
        );


        /*
         * 1. Find cluster
         */
        Cluster cluster = clusterRepository.findById(clusterId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.CLUSTER_NOT_FOUND,
                        "Cluster not found."
                ));


        /*
         * 2. Validate kubeconfig
         */
        if (cluster.getEncryptedKubeConfig() == null ||
                cluster.getEncryptedKubeConfig().isBlank()) {

            throw new BadRequestException(
                    ErrorCode.INVALID_CLUSTER_CONFIGURATION,
                    "Cluster kubeconfig is not available."
            );
        }


        /*
         * 3. Validate request
         */
        if (request == null ||
                request.getCommand() == null ||
                request.getCommand().isEmpty()) {

            throw new BadRequestException(
                    ErrorCode.POD_EXECUTION_FAILED,
                    "Command is required."
            );
        }


        try (KubernetesClient client =
                     kubernetesClientFactory.createClient(
                             cluster.getEncryptedKubeConfig()
                     )) {


            /*
             * 4. Find Pod
             */
            Pod pod = client.pods()
                    .inNamespace(namespace)
                    .withName(podName)
                    .get();


            if (pod == null) {

                throw new ResourceNotFoundException(
                        ErrorCode.POD_NOT_FOUND,
                        "Pod not found."
                );
            }


            /*
             * 5. Validate containers
             */
            if (pod.getSpec() == null ||
                    pod.getSpec().getContainers() == null ||
                    pod.getSpec().getContainers().isEmpty()) {

                throw new BadRequestException(
                        ErrorCode.POD_EXECUTION_FAILED,
                        "Pod does not contain any containers."
                );
            }


            /*
             * 6. Select container
             *
             * If no container is provided,
             * use the first container.
             */
            String selectedContainer = request.getContainer();

            if (selectedContainer == null ||
                    selectedContainer.isBlank()) {

                selectedContainer = pod.getSpec()
                        .getContainers()
                        .get(0)
                        .getName();
            }


            /*
             * 7. Validate selected container
             */
            String finalContainer = selectedContainer;

            boolean containerExists = pod.getSpec()
                    .getContainers()
                    .stream()
                    .anyMatch(container ->
                            finalContainer.equals(container.getName())
                    );


            if (!containerExists) {

                throw new BadRequestException(
                        ErrorCode.POD_EXECUTION_FAILED,
                        "Container '" +
                                selectedContainer +
                                "' not found in pod."
                );
            }


            /*
             * 8. Prepare output streams
             */
            ByteArrayOutputStream stdout =
                    new ByteArrayOutputStream();

            ByteArrayOutputStream stderr =
                    new ByteArrayOutputStream();


            /*
             * 9. Completion latch
             */
            CountDownLatch completed =
                    new CountDownLatch(1);


            /*
             * 10. Fabric8 Exec Listener
             *
             * This signature is for the newer Fabric8 API.
             */
            ExecListener listener = new ExecListener() {

                @Override
                public void onOpen() {

                    log.debug(
                            "Exec connection opened for pod '{}'.",
                            podName
                    );
                }


                @Override
                public void onFailure(
                        Throwable throwable,
                        ExecListener.Response response
                ) {

                    log.error(
                            "Command execution failed in pod '{}'.",
                            podName,
                            throwable
                    );

                    completed.countDown();
                }


                @Override
                public void onClose(
                        int code,
                        String reason
                ) {

                    log.debug(
                            "Exec connection closed. code={}, reason={}",
                            code,
                            reason
                    );

                    completed.countDown();
                }
            };


            ExecWatch execWatch = null;


            try {

                /*
                 * 11. Execute command
                 */
                execWatch = client.pods()
                        .inNamespace(namespace)
                        .withName(podName)
                        .inContainer(selectedContainer)
                        .writingOutput(stdout)
                        .writingError(stderr)
                        .usingListener(listener)
                        .exec(
                                request.getCommand()
                                        .toArray(new String[0])
                        );


                /*
                 * 12. Wait for command completion
                 */
                boolean finished = completed.await(
                        30,
                        TimeUnit.SECONDS
                );


                if (!finished) {

                    throw new BadRequestException(
                            ErrorCode.POD_EXECUTION_FAILED,
                            "Command execution timed out."
                    );
                }


            } finally {

                /*
                 * 13. Always close ExecWatch
                 */
                if (execWatch != null) {
                    execWatch.close();
                }
            }


            /*
             * 14. Convert output
             */
            String output =
                    stdout.toString(StandardCharsets.UTF_8);

            String error =
                    stderr.toString(StandardCharsets.UTF_8);


            /*
             * 15. Build response
             */
            return PodExecResponse.builder()
                    .podName(podName)
                    .namespace(namespace)
                    .containerName(selectedContainer)
                    .command(
                            String.join(
                                    " ",
                                    request.getCommand()
                            )
                    )
                    .output(output)
                    .error(error)
                    .build();


        } catch (ResourceNotFoundException exception) {

            throw exception;


        } catch (BadRequestException exception) {

            throw exception;


        } catch (Exception exception) {

            log.error(
                    "Failed to execute command in pod '{}'.",
                    podName,
                    exception
            );

            throw new BadRequestException(
                    ErrorCode.POD_EXECUTION_FAILED,
                    "Unable to execute command in pod."
            );
        }
    }
}