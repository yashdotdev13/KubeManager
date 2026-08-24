package com.kubemanager.ai_service.rag;


import com.kubemanager.ai_service.rag.knowledge.KubernetesKnowledgeIngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class KubernetesKnowledgeIngestionIntegrationTest {

    @Autowired
    private KubernetesKnowledgeIngestionService ingestionService;

    @Autowired
    private VectorStore vectorStore;

    @Test
    void shouldIngestKubernetesKnowledge() {

        ingestionService.ingestKnowledge();

        List<Document> results =
                vectorStore.similaritySearch(
                        SearchRequest.builder()
                                .query(
                                        "Kubernetes CrashLoopBackOff troubleshooting"
                                )
                                .topK(5)
                                .similarityThreshold(0.0)
                                .build()
                );

        assertNotNull(results);

        assertFalse(
                results.isEmpty(),
                "Expected Kubernetes knowledge to be retrievable."
        );

        System.out.println(
                "\n========== KUBERNETES KNOWLEDGE =========="
        );

        System.out.println(
                "Retrieved documents: "
                        + results.size()
        );

        for (Document document : results) {

            System.out.println(
                    "\n--------------------------------------"
            );

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
        }

        System.out.println(
                "==========================================\n"
        );
    }
}