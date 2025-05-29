package com.bigteam.aichat.rag.processor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;


import lombok.extern.slf4j.Slf4j;

/**
 * PDF 문서 처리 전략 구현
 * 다중 컬럼 레이아웃을 포함한 PDF 문서를 처리합니다.
 */
@Slf4j
public class PdfDocumentProcessingStrategy implements DocumentProcessingStrategy {
    
    private final PdfDocumentReaderConfig config;
    
    public PdfDocumentProcessingStrategy() {
        this.config = PdfDocumentReaderConfig.builder()
                .withPageTopMargin(0)
                .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                        .withNumberOfTopTextLinesToDelete(0)
                        .build())
                .withPagesPerDocument(1)
                .build();
        
    }
    
    @Override
    public List<Document> parse(Resource resource) throws IOException {
        log.info("PDF 데이터 로드 시작: {}", resource.getFilename());
        try {
            ParagraphPdfDocumentReader pdfReader = new ParagraphPdfDocumentReader(resource, this.config);
            return pdfReader.get();
        } catch (Exception e) {
            log.error("Paragraph 기반 Pdf 데이터 로드 실패...{}\n===Page 기반 Pdf 데이터 로드 시도===", e.getMessage());
            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource, this.config);
            return pdfReader.get();
        }
    }
    
    @Override
    public List<Document> clean(List<Document> documents) {
        return documents.stream()
                .map(doc -> 
                    new Document(Objects.requireNonNull(doc.getText())
                        .replaceAll("\\s+", " ")  // 여러 개의 공백을 하나로 축소
                        .replaceAll(",\\s*", ", ")  // 쉼표 뒤에 한 칸 공백 추가
                        .trim(), doc.getMetadata())
                )
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Document> chunk(List<Document> documents, int chunkSize, int chunkOverlap) {
        TokenTextSplitter splitter = new TokenTextSplitter(
            chunkSize,
            350,  // minChunkSizeChars
            5,    // minChunkLengthToEmbed
            10000, // maxNumChunks
            true   // keepSeparator
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
        return filename != null && filename.toLowerCase().endsWith(".pdf");
    }
}

