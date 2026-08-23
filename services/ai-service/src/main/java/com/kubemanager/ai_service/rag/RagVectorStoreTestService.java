package com.kubemanager.ai_service.rag;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RagVectorStoreTestService {

    private final VectorStore vectorStore;

    public void insertTestDocument() {
        Document document = new Document(
                """
                Kubernetes CrashLoopBackOff occurs when a container
                repeatedly starts and then terminates unsuccessfully.
                Common causes include application crashes, incorrect
                configuration, missing environment variables, failed
                health checks, and unavailable dependencies.
                """,
                Map.of(
                        "source", "kubernetes-troubleshooting",
                        "type", "test"
                )
        );
        vectorStore.add(List.of(document));
    }

    public List<Document> search(String query) {
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(3)
                        .similarityThreshold(0.0)
                        .build()
        );
    }
}