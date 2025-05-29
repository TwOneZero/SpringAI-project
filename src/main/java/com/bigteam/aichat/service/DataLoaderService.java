package com.bigteam.aichat.service;

import com.bigteam.aichat.entity.DocumentInfo;
import com.bigteam.aichat.rag.processor.DocumentProcessingStrategy;
import com.bigteam.aichat.rag.processor.DocumentProcessingStrategyFactory;
import com.bigteam.aichat.repository.DocumentInfoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * RAG 파이프라인을 위한 데이터 로딩 서비스
 * 다양한 문서 형식을 처리하고 벡터 저장소에 저장합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataLoaderService {
    private final VectorStore vectorStore;
    private final DocumentInfoRepository documentInfoRepository;
    private final DocumentProcessingStrategyFactory strategyFactory;
    private final SummaryMetadataEnricher enricher;
    
    @Value("${rag.document.chunk-size:800}")
    private int chunkSize;
    
    @Value("${rag.document.chunk-overlap:200}")
    private int chunkOverlap;

    /**
     * 파일을 업로드하고 RAG 파이프라인을 위한 처리를 수행합니다.
     * 
     * @param file   업로드된 파일
     * @param userId 사용자 ID
     * @param chatId 채팅 ID
     * @return 생성된 문서 정보
     * @throws IOException 파일 처리 중 발생한 예외
     */
    @Transactional
    public DocumentInfo processAndStoreDocument(MultipartFile file, String userId) throws IOException {
        log.info("문서 처리 시작: {}, 사용자: {}, 채팅 ID: {}", file.getOriginalFilename(), userId);
        
        // 1. 문서 정보 데이터베이스에 저장
        DocumentInfo docInfo = saveDocumentInfo(file, userId);
        log.info("문서 정보 저장 완료, 문서 ID: {}", docInfo.getId());
        
        // 2. 파일을 Document 객체로 변환
        Resource resource = file.getResource();
        List<Document> documents = processFileToDocuments(resource, docInfo);
        log.info("문서 변환 완료, 생성된 문서 조각 수: {}", documents.size());
        
        // 3. 벡터 저장소에 문서 추가
        vectorStore.add(documents);
        log.info("벡터 저장소에 문서 저장 완료");
        
        return docInfo;
    }

    /**
     * 문서 정보를 저장합니다.
     * 
     * @param file   업로드된 파일
     * @param userId 사용자 ID
     * @param chatId 채팅 ID
     * @return 저장된 문서 정보
     */
    private DocumentInfo saveDocumentInfo(MultipartFile file, String userId) {
        return documentInfoRepository.save(DocumentInfo.builder()
                .userId(userId)
                .filename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .onChat(true)
                .build());
    }

    /**
     * 파일을 Document 객체로 처리합니다.
     * 
     * @param resource 처리할 리소스
     * @param docInfo  문서 정보
     * @return 처리된 Document 목록
     * @throws IOException 파일 처리 중 발생한 예외
     */
    private List<Document> processFileToDocuments(Resource resource, DocumentInfo docInfo) throws IOException {
        log.info("파일 처리 시작: {}", resource.getFilename());

        // 파일 형식에 적합한 처리 전략 선택
        DocumentProcessingStrategy strategy = strategyFactory.getStrategy(resource);

        // 메타데이터 준비
        Map<String, Object> metadata = prepareMetadata(docInfo);

        // 처리 파이프라인: 파싱 -> 정제 -> 청킹 -> 메타데이터 보강
        List<Document> parsedDocs = strategy.parse(resource);
        log.info("문서 파싱 완료, 파싱된 문서 수: {}", parsedDocs.size());

        List<Document> cleanedDocs = strategy.clean(parsedDocs);
        log.info("문서 정제 완료");

        List<Document> chunkedDocs = strategy.chunk(cleanedDocs, chunkSize, chunkOverlap);
        log.info("문서 분할 완료, 최종 청크 수: {}", chunkedDocs.size());

        List<Document> preparedDocs = strategy.enrichMetadata(chunkedDocs, metadata);

        return enrichDocuments(preparedDocs);
    }
    
    /**
     * 문서에 메타데이터를 추가합니다.
     *
     * @param documents 메타데이터를 추가할 문서 목록
     * @return 메타데이터가 추가된 문서 목록
     */
    public List<Document> enrichDocuments(List<Document> documents) {
        return enricher.apply(documents);
    }
    
    /**
     * 문서 메타데이터를 준비합니다.
     * 
     * @param docInfo 문서 정보
     * @return 메타데이터 맵
     */
    private Map<String, Object> prepareMetadata(DocumentInfo docInfo) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("document_id", docInfo.getId());
        metadata.put("user_id", docInfo.getUserId());
        metadata.put("file_name", docInfo.getFilename());
        return metadata;
    }
    
}