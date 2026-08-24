package com.kubemanager.ai_service.rag.ingestion;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RagDocumentChunker {

    private final TokenTextSplitter textSplitter;

    public List<Document> chunk(Document document) {

        if (document == null) {
            throw new IllegalArgumentException(
                    "Document cannot be null."
            );
        }

        return textSplitter.apply(
                List.of(document)
        );
    }
}