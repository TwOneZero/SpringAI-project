package com.bigteam.aichat.rag;

import com.bigteam.aichat.rag.module.CustomDocumentProcessor;
import com.bigteam.aichat.repository.DocumentInfoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 메모리가 작은 모델을 위한 개선된 RAG 서비스
 * Spring AI의 모듈형 RAG 아키텍처를 활용하여 검색 품질 향상
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnhancedDocumentRagService {

    private final VectorStore vectorStore;
    private final DocumentInfoRepository documentInfoRepository;
    // private final QueryTransformer queryTransformer;
    private final QueryAugmenter queryAugmenter;
    private final MultiQueryExpander multiQueryExpander;
    private final CustomDocumentProcessor customDocumentProcessor;


    public List<Long> getActiveDocFilter(String userId) {
        List<Long> activeDocIds = documentInfoRepository.findActiveDocumentIdsByUserId(userId);
        if (activeDocIds.isEmpty()) {
            return Collections.emptyList();
        } else {
            return activeDocIds;
        }
    }

    public Advisor getRagAdvisor() {
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.5)
                        .topK(5)
                        .vectorStore(vectorStore)                        
                        .build())
                // .queryTransformers(queryTransformer)
                .queryExpander(multiQueryExpander)
                .documentPostProcessors(customDocumentProcessor)
                .queryAugmenter(queryAugmenter)
                .build();        
    }

}