package com.kubemanager.ai_service.rag.config;


import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RagChunkingConfig {

    @Bean
    public TokenTextSplitter tokenTextSplitter() {

        return TokenTextSplitter.builder()
                .withChunkSize(500)
                .withMinChunkSizeChars(200)
                .withMinChunkLengthToEmbed(50)
                .withMaxNumChunks(100)
                .build();
    }
}