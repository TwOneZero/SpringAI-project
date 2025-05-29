package com.bigteam.aichat.rag.processor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

public interface DocumentProcessingStrategy {
    /**
     * 문서 파싱을 수행합니다.
     * @param resource 처리할 문서 리소스
     * @return 파싱된 도큐먼트 목록
     * @throws IOException I/O 예외 발생 시
     */
    List<Document> parse(Resource resource) throws IOException;
    
    /**
     * 문서 텍스트를 정제합니다.
     * @param documents 정제할 도큐먼트 목록
     * @return 정제된 도큐먼트 목록
     */
    List<Document> clean(List<Document> documents);
    
    /**
     * 문서를 적절한 크기로 분할합니다.
     * @param documents 분할할 도큐먼트 목록
     * @param chunkSize 청크 크기
     * @param chunkOverlap 청크 간 중복 크기
     * @return 분할된 도큐먼트 목록
     */
    List<Document> chunk(List<Document> documents, int chunkSize, int chunkOverlap);
    
    /**
     * 문서 메타데이터를 보강합니다.
     * @param documents 도큐먼트 목록
     * @param additionalMetadata 추가할 메타데이터
     * @return 메타데이터가 보강된 도큐먼트 목록
     */
    List<Document> enrichMetadata(List<Document> documents, Map<String, Object> additionalMetadata);
    
    /**
     * 이 전략이 특정 리소스를 처리할 수 있는지 확인합니다.
     * @param resource 확인할 리소스
     * @return 처리 가능 여부
     */
    boolean canProcess(Resource resource);
}