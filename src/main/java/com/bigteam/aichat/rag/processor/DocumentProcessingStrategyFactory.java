package com.bigteam.aichat.rag.processor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 문서 처리 전략 팩토리
 * 문서 유형에 따라 적절한 처리 전략을 제공합니다.
 */
@Slf4j
@Component
public class DocumentProcessingStrategyFactory {
    
    private final List<DocumentProcessingStrategy> strategies;
    
    public DocumentProcessingStrategyFactory() {
        this.strategies = new ArrayList<>();
        // 전략 등록
        this.strategies.add(new PdfDocumentProcessingStrategy());
        this.strategies.add(new TikaDocumentProcessingStrategy());
        this.strategies.add(new TextDocumentProcessingStrategy());
        this.strategies.add(new JsonDocumentProcessingStrategy());
    }
    
    /**
     * 주어진 리소스에 적합한 문서 처리 전략을 반환합니다.
     * @param resource 처리할 리소스
     * @return 문서 처리 전략
     * @throws IllegalArgumentException 적합한 전략을 찾을 수 없을 경우
     */
    public DocumentProcessingStrategy getStrategy(Resource resource) {
        for (DocumentProcessingStrategy strategy : this.strategies) {
            if (strategy.canProcess(resource)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("지원되지 않는 파일 형식: " + resource.getFilename());
    }
    
    /**
     * 새로운 문서 처리 전략을 등록합니다.
     * @param strategy 등록할 전략
     */
    public void registerStrategy(DocumentProcessingStrategy strategy) {
        this.strategies.add(strategy);
    }
}