package com.kubemanager.ai_service.rag;

import com.kubemanager.ai_service.rag.ingestion.RagIngestionService;
import com.kubemanager.ai_service.rag.retrieval.RagRetrievalService;

import org.junit.jupiter.api.Test;

import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class RagRetrievalIntegrationTest {

    @Autowired
    private RagIngestionService ragIngestionService;

    @Autowired
    private RagRetrievalService ragRetrievalService;

    @Test
    void shouldRetrieveKubernetesKnowledge() {

        /*
         * 1. Arrange
         *
         * Insert deterministic Kubernetes knowledge
         * into the RAG vector store.
         */

        RagDocument knowledge =
                RagDocument.builder()
                        .content("""
                                Kubernetes CrashLoopBackOff occurs when
                                a container repeatedly starts and then
                                terminates unsuccessfully.

                                Common causes include application crashes,
                                incorrect configuration, missing environment
                                variables, failed health checks, and
                                unavailable dependencies.

                                To troubleshoot CrashLoopBackOff, inspect
                                the pod status, container logs, previous
                                container logs, events, configuration,
                                environment variables, and dependent
                                services.
                                """)
                        .source("kubernetes-troubleshooting")
                        .documentType("KUBERNETES_KNOWLEDGE")
                        .metadata(Map.of(
                                "topic", "pod-lifecycle",
                                "category", "troubleshooting",
                                "resourceType", "Pod"
                        ))
                        .build();

        /*
         * 2. Ingest knowledge
         */

        ragIngestionService.ingest(
                knowledge
        );

        /*
         * 3. Retrieve using a semantic query
         */

        List<Document> results =
                ragRetrievalService.retrieve(
                        "Why does my Kubernetes pod keep restarting?"
                );

        /*
         * 4. Validate retrieval
         */

        assertNotNull(results);

        assertFalse(
                results.isEmpty(),
                "Expected relevant Kubernetes knowledge."
        );

        /*
         * 5. Print retrieved knowledge
         */

        System.out.println(
                "========== RAG RETRIEVAL =========="
        );

        results.forEach(document -> {

            System.out.println(
                    "CONTENT:"
            );

            System.out.println(
                    document.getText()
            );

            System.out.println(
                    "METADATA:"
            );

            System.out.println(
                    document.getMetadata()
            );

            System.out.println(
                    "----------------------------------"
            );
        });

        System.out.println(
                "Retrieved documents: "
                        + results.size()
        );
    }
}