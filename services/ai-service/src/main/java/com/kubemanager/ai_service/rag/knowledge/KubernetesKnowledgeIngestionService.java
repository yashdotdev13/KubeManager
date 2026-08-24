package com.kubemanager.ai_service.rag.knowledge;

import com.kubemanager.ai_service.rag.RagDocument;
import com.kubemanager.ai_service.rag.ingestion.RagIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KubernetesKnowledgeIngestionService {

    private final KubernetesKnowledgeBase knowledgeBase;
    private final RagIngestionService ragIngestionService;

    public void ingestKnowledge() {

        List<String> documents =
                knowledgeBase.getDocuments();

        log.info(
                "Starting Kubernetes knowledge ingestion. documents={}",
                documents.size()
        );

        for (int i = 0; i < documents.size(); i++) {

            String content = documents.get(i);

            RagDocument document =
                    RagDocument.builder()
                            .content(content)
                            .source(
                                    "kubernetes-knowledge-" + (i + 1)
                            )
                            .documentType(
                                    "KUBERNETES_KNOWLEDGE"
                            )
                            .metadata(
                                    Map.of(
                                            "source",
                                            "kubernetes-knowledge",

                                            "category",
                                            "kubernetes",

                                            "documentIndex",
                                            i + 1
                                    )
                            )
                            .build();

            ragIngestionService.ingest(document);

            log.info(
                    "Kubernetes knowledge document ingested. index={}/{}",
                    i + 1,
                    documents.size()
            );
        }

        log.info(
                "Kubernetes knowledge ingestion completed. documents={}",
                documents.size()
        );
    }
}