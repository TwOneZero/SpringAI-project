package com.bigteam.aichat.rag.processor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * Tika 기반 문서 처리 전략 구현
 * MS Office 문서 등 다양한 형식의 문서를 처리합니다.
 */
@Slf4j
public class TikaDocumentProcessingStrategy implements DocumentProcessingStrategy {
    
    @Override
    public List<Document> parse(Resource resource) throws IOException {
        log.info("Tika 기반 데이터 로드 시작: {}", resource.getFilename());
        TikaDocumentReader tikaReader = new TikaDocumentReader(resource);
        return tikaReader.read();
    }
    
    @Override
    public List<Document> clean(List<Document> documents) {
        return documents.stream()
                .map(doc -> 
                    new Document(Objects.requireNonNull(doc.getText())
                        .replaceAll("\\s+", " ")
                        .replaceAll("(?<=[a-zA-Z가-힣])\\.\\s*", ".\n")
                        .replaceAll(",\\s*", ", ")
                        .trim(), doc.getMetadata())
                )
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Document> chunk(List<Document> documents, int chunkSize, int chunkOverlap) {
        TokenTextSplitter splitter = new TokenTextSplitter(
            chunkSize,
            350,
            5,
            10000,
            true
        );
        return splitter.apply(documents);
    }
    
    @Override
    public List<Document> enrichMetadata(List<Document> documents, Map<String, Object> additionalMetadata) {
        return documents.stream()
                .map(doc -> {
                    Map<String, Object> metadata = doc.getMetadata();
                    metadata.putAll(additionalMetadata);
                    return new Document(Objects.requireNonNull(doc.getText()), metadata);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean canProcess(Resource resource) {
        String filename = resource.getFilename();
        if (filename == null) return false;
        
        String lowercaseName = filename.toLowerCase();
        return lowercaseName.endsWith(".docx") || 
               lowercaseName.endsWith(".pptx") ||
               lowercaseName.endsWith(".xlsx") ||
               lowercaseName.endsWith(".doc") ||
               lowercaseName.endsWith(".ppt") ||
               lowercaseName.endsWith(".xls") ||
               lowercaseName.endsWith(".html") ||
               lowercaseName.endsWith(".htm");
    }
}