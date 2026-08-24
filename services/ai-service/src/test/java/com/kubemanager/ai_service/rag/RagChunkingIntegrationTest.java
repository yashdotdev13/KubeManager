package com.kubemanager.ai_service.rag;

import com.kubemanager.ai_service.rag.ingestion.RagDocumentChunker;
import com.kubemanager.ai_service.rag.ingestion.RagIngestionService;

import org.junit.jupiter.api.Test;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class RagChunkingIntegrationTest {

    @Autowired
    private RagDocumentChunker ragDocumentChunker;

    @Autowired
    private RagIngestionService ragIngestionService;

    @Autowired
    private VectorStore vectorStore;

    @Test
    void shouldSplitLargeKubernetesKnowledgeIntoChunks() {

        String knowledge = """
                Kubernetes Pods are the smallest deployable units
                of computing that you can create and manage in Kubernetes.
                A Pod represents a running process in your cluster.

                A Pod can contain one or more containers.
                Containers inside the same Pod share networking and
                storage resources. Pods are generally managed through
                higher-level Kubernetes resources such as Deployments,
                StatefulSets, DaemonSets, and Jobs.

                Kubernetes Pod lifecycle includes several important phases.
                A Pod may be Pending, Running, Succeeded, Failed, or Unknown.
                The phase provides a high-level summary of the Pod lifecycle.

                CrashLoopBackOff occurs when a container repeatedly starts
                and terminates unsuccessfully. Kubernetes progressively
                increases the delay between restart attempts.

                Common causes of CrashLoopBackOff include application
                crashes, incorrect environment variables, invalid
                configuration, missing Secrets, missing ConfigMaps,
                unavailable databases, failed health checks, and incorrect
                container startup commands.

                When investigating CrashLoopBackOff, inspect the Pod status
                first. The kubectl describe pod command provides useful
                information about container state, events, scheduling,
                probes, volumes, and configuration.

                Container logs are another important diagnostic source.
                The kubectl logs command displays the current container logs.
                If the container has already restarted, previous container
                logs can be retrieved using the --previous option.

                Kubernetes readiness probes determine whether a container
                is ready to receive traffic. A failed readiness probe
                removes the Pod from the endpoints of the associated Service.

                Kubernetes liveness probes determine whether a container
                is still healthy. Repeated liveness probe failures can cause
                Kubernetes to restart the container.

                Kubernetes startup probes are useful for applications that
                require significant startup time. Startup probes prevent
                liveness and readiness probes from running too early.

                ConfigMaps provide a mechanism for storing non-sensitive
                configuration data. Secrets are intended for sensitive
                configuration such as passwords, tokens, and credentials.

                Resource requests and limits control how Kubernetes schedules
                and constrains containers. Requests influence scheduling,
                while limits constrain resource consumption.

                Kubernetes Services provide stable networking endpoints for
                applications running inside the cluster. Services select
                Pods using labels and forward traffic to matching endpoints.

                When a Pod cannot communicate with another service, inspect
                Service selectors, EndpointSlices, DNS resolution,
                NetworkPolicies, container ports, and application listeners.

                Kubernetes Events provide useful information about scheduling,
                image pulling, container creation, probe failures, mounting
                problems, and other lifecycle events.

                ImagePullBackOff indicates that Kubernetes is unable to pull
                a required container image. Common causes include incorrect
                image names, unavailable registries, authentication failures,
                and image tags that do not exist.

                When debugging Kubernetes workloads, always correlate Pod
                status, container logs, previous logs, events, configuration,
                probes, resources, networking, and dependent services.
                """;

        /*
         * =========================================================
         * 2. Create RagDocument
         * =========================================================
         */

        RagDocument ragDocument =
                RagDocument.builder()
                        .content(knowledge)
                        .source("kubernetes-core-knowledge")
                        .documentType("KUBERNETES_KNOWLEDGE")
                        .metadata(
                                Map.of(
                                        "source",
                                        "kubernetes-core-knowledge",

                                        "category",
                                        "kubernetes",

                                        "version",
                                        "1"
                                )
                        )
                        .build();

        /*
         * =========================================================
         * 3. Convert RagDocument to Spring AI Document
         * =========================================================
         */

        Document springDocument =
                new Document(
                        ragDocument.getContent(),
                        ragDocument.getMetadata()
                );

        /*
         * =========================================================
         * 4. Test chunking directly
         *
         * We intentionally test the chunker itself here.
         * We do NOT use similarity-search result count to
         * determine how many chunks were generated.
         * =========================================================
         */

        List<Document> chunks =
                ragDocumentChunker.chunk(
                        springDocument
                );

        /*
         * =========================================================
         * 5. Validate chunking
         * =========================================================
         */

        assertNotNull(
                chunks,
                "Chunks must not be null."
        );

        assertFalse(
                chunks.isEmpty(),
                "Expected at least one chunk."
        );

        assertTrue(
                chunks.size() > 1,
                "Expected large document to produce multiple chunks."
        );

        /*
         * =========================================================
         * 6. Print chunks
         * =========================================================
         */

        System.out.println(
                "\n========== CHUNKING RESULTS =========="
        );

        System.out.println(
                "Original document length: "
                        + knowledge.length()
        );

        System.out.println(
                "Generated chunks: "
                        + chunks.size()
        );

        for (int i = 0; i < chunks.size(); i++) {

            Document chunk =
                    chunks.get(i);

            System.out.println(
                    "\n--------------------------------------"
            );

            System.out.println(
                    "CHUNK "
                            + (i + 1)
                            + " / "
                            + chunks.size()
            );

            System.out.println(
                    "CONTENT:"
            );

            System.out.println(
                    chunk.getText()
            );

            System.out.println(
                    "METADATA:"
            );

            System.out.println(
                    chunk.getMetadata()
            );
        }

        System.out.println(
                "======================================\n"
        );

        /*
         * =========================================================
         * 7. Ingest the original RagDocument
         *
         * The ingestion service performs:
         *
         * RagDocument
         *      ↓
         * Spring AI Document
         *      ↓
         * RagDocumentChunker
         *      ↓
         * Chunks
         *      ↓
         * Gemini Embeddings
         *      ↓
         * PGVector
         * =========================================================
         */

        ragIngestionService.ingest(
                ragDocument
        );

        /*
         * =========================================================
         * 8. Retrieve the ingested knowledge
         * =========================================================
         */

        List<Document> results =
                vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(
                                        "Kubernetes CrashLoopBackOff troubleshooting"
                                )
                                .topK(10)
                                .similarityThreshold(0.0)
                                .build()
                );

        /*
         * =========================================================
         * 9. Validate retrieval
         *
         * We only verify that relevant knowledge can be retrieved.
         *
         * We DO NOT assert results.size() > 1 because similarity
         * search is not expected to return every stored chunk.
         * =========================================================
         */

        assertNotNull(
                results,
                "Retrieval results must not be null."
        );

        assertFalse(
                results.isEmpty(),
                "Expected Kubernetes knowledge to be retrievable."
        );

        /*
         * =========================================================
         * 10. Print retrieval results
         * =========================================================
         */

        System.out.println(
                "\n========== RETRIEVAL RESULTS =========="
        );

        System.out.println(
                "Retrieved documents: "
                        + results.size()
        );

        for (int i = 0; i < results.size(); i++) {

            Document result =
                    results.get(i);

            System.out.println(
                    "\n--------------------------------------"
            );

            System.out.println(
                    "RESULT "
                            + (i + 1)
            );

            System.out.println(
                    "CONTENT:"
            );

            System.out.println(
                    result.getText()
            );

            System.out.println(
                    "METADATA:"
            );

            System.out.println(
                    result.getMetadata()
            );
        }

        System.out.println(
                "======================================\n"
        );
    }
}