package com.kubemanager.ai_service.rag.ingestion;


import com.kubemanager.ai_service.rag.RagDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagIngestionServiceImpl implements RagIngestionService {

    private final VectorStore vectorStore;

    private final RagDocumentChunker documentChunker;

    @Override
    public void ingest(RagDocument ragDocument) {

        if (ragDocument == null) {
            throw new IllegalArgumentException(
                    "RAG document cannot be null."
            );
        }

        if (ragDocument.getContent() == null
                || ragDocument.getContent().isBlank()) {

            throw new IllegalArgumentException(
                    "RAG document content cannot be blank."
            );
        }

        Document document = new Document(
                ragDocument.getContent(),
                ragDocument.getMetadata()
        );

        List<Document> chunks =
                documentChunker.chunk(document);

        if (chunks.isEmpty()) {

            throw new IllegalStateException(
                    "No chunks generated from RAG document."
            );
        }

        vectorStore.add(
                List.of(document)
        );
        log.info(
                "RAG document ingested successfully. source={}, type={}",
                ragDocument.getSource(),
                ragDocument.getDocumentType()
        );
    }
}