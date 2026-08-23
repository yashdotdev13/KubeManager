package com.kubemanager.ai_service.config;


import com.zaxxer.hikari.HikariDataSource;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class RagVectorStoreConfig {

    @Bean
    @ConfigurationProperties("rag.datasource")
    public DataSourceProperties ragDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("rag.datasource.hikari")
    public DataSource ragDataSource(
            DataSourceProperties ragDataSourceProperties
    ) {

        return ragDataSourceProperties
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    public JdbcTemplate ragJdbcTemplate(
            DataSource ragDataSource
    ) {

        return new JdbcTemplate(
                ragDataSource
        );
    }

    @Bean
    public VectorStore ragVectorStore(
            JdbcTemplate ragJdbcTemplate,
            EmbeddingModel embeddingModel
    ) {

        return PgVectorStore
                .builder(
                        ragJdbcTemplate,
                        embeddingModel
                )
                .dimensions(768)
                .distanceType(
                        PgDistanceType.COSINE_DISTANCE
                )
                .indexType(
                        PgIndexType.HNSW
                )
                .initializeSchema(true)
                .schemaName("public")
                .vectorTableName("vector_store")
                .maxDocumentBatchSize(1000)
                .build();
    }
}