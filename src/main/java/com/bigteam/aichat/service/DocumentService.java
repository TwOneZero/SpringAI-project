package com.bigteam.aichat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bigteam.aichat.entity.DocumentInfo;
import com.bigteam.aichat.repository.DocumentInfoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentInfoRepository documentInfoRepository;
    
    
    /**
     * 사용자 ID에 해당하는 모든 문서를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 문서 정보 목록
     */
    public List<DocumentInfo> getUserDocuments(String userId) {
        return documentInfoRepository.findByUserId(userId);
    }
    
    /**
     * 문서 ID 목록에 해당하는 문서를 데이터베이스와 벡터 저장소에서 삭제합니다.
     * 
     * @param documentIds 삭제할 문서 ID 목록
     */
    @Transactional
    public void deleteDocuments(List<Long> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            log.info("삭제할 문서가 없습니다.");
            return;
        }

        log.info("문서 삭제 시작, 문서 ID: {}", documentIds);

        // 2. 데이터베이스에서 문서 정보 삭제
        documentInfoRepository.deleteAllByIds(documentIds);

        log.info("문서 삭제 완료");
    }

    /**
     * 여러 문서의 활성화 상태를 일괄 변경합니다.
     * 
     * @param userId     사용자 ID
     * @param documentIds 문서 ID 목록
     * @param isActive   활성화 여부
     * @return 업데이트된 문서 정보 목록
     */
    @Transactional
    public List<DocumentInfo> toggleDocumentsActive(String userId, List<Long> documentIds, boolean isActive) {
        log.info("문서 일괄 활성화 상태 변경: 문서 ID: {}, 유저 ID: {}, 활성화 여부: {}", documentIds, userId, isActive);
        
        List<DocumentInfo> documents = documentInfoRepository.findAllById(documentIds);
        
        // 모든 문서가 해당 사용자의 것인지 확인
        documents.forEach(doc -> {
            if (!doc.getUserId().equals(userId)) {
                throw new IllegalArgumentException("해당 사용자의 문서가 아닙니다: " + doc.getId());
            }
            doc.setOnChat(isActive);
        });
        
        return documentInfoRepository.saveAll(documents);
    }
}
