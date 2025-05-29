package com.bigteam.aichat.rag.module;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomDocumentProcessor implements DocumentPostProcessor {

    @Override
    public List<Document> process(Query query, List<Document> documents) {

        //문서 로그
        documents.forEach(doc -> {
            log.info("문서 ID: {}, 페이지 번호: {}, 유사도 점수: {}",
                    doc.getMetadata().getOrDefault("document_id", "Unknown"),
                    doc.getMetadata().getOrDefault("page_number", "Unknown"),
                    doc.getScore());
        });

        // 중복 내용 제거 및 정렬
        Map<String, Document> uniqueContentMap = new HashMap<>();
        for (Document doc : documents) {
            String content = doc.getFormattedContent();
            // 기존 문서보다 유사도 점수가 높은 경우만 교체
            if (!uniqueContentMap.containsKey(content) ||
                    doc.getScore() > uniqueContentMap.get(content).getScore()) {
                uniqueContentMap.put(content, doc);
            }
        }

        // 유사도 점수 기준 내림차순 정렬
        return uniqueContentMap.values().stream()
                .sorted(Comparator.comparing(Document::getScore).reversed())
                .collect(Collectors.toList());
    }

}
