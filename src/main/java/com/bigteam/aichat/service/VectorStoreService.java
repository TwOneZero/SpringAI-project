package com.bigteam.aichat.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final VectorStore vectorStore;

     /**
     * 벡터 저장소에서 문서를 삭제합니다.
     * 
     * @param docIds 삭제할 문서 ID 목록
     */
    @Transactional
    public void removeDocumentsFromVectorStore(List<Long> docIds) {
        if (docIds == null || docIds.isEmpty()) {
            log.info("삭제할 문서가 없습니다.");
            return;
        }
        
        log.info("벡터 저장소에서 문서 삭제 시작, 문서 ID: {}", docIds);
        
        String filterExpression = "document_id in [" + 
                docIds.stream()
                      .map(id -> "'" + id + "'")
                      .collect(Collectors.joining(", ")) + 
                "]";
        
        try {
            vectorStore.delete(filterExpression);
            log.info("벡터 저장소에서 문서 삭제 완료");
        } catch (Exception e) {
            log.error("벡터 저장소에서 문서 삭제 중 오류 발생", e);
            throw new RuntimeException("벡터 저장소에서 문서 삭제 실패", e);
        }
    }
}
