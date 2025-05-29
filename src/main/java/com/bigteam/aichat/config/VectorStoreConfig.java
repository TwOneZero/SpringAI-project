// package com.bigteam.aichat.config;

// import org.springframework.ai.embedding.EmbeddingModel;
// import org.springframework.ai.vectorstore.VectorStore;
// import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.jdbc.core.JdbcTemplate;

// @Configuration
// public class VectorStoreConfig {

// 	@Bean
// 	public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
// 		return PgVectorStore.builder(jdbcTemplate,embeddingModel)
// //							.dimensions(1024)                    // Optional: defaults to model dimensions or 1536
// 							.distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)       // Optional: defaults to COSINE_DISTANCE
// 							.indexType(PgVectorStore.PgIndexType.HNSW)                     // Optional: defaults to HNSW
// 							.initializeSchema(true)              // Optional: defaults to false
// 							.schemaName("public")                // Optional: defaults to "public"
// 							.vectorTableName("vector_store")     // Optional: defaults to "vector_store"
// 							.maxDocumentBatchSize(10000)         // Optional: defaults to 10000
// 							.removeExistingVectorStoreTable(false)
// 							.build();
// 	}
// }
