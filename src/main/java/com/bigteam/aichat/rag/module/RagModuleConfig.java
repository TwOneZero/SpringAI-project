package com.bigteam.aichat.rag.module;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Configuration
public class RagModuleConfig {


    @Value("classpath:prompts/rag-prompt.txt")
    private Resource ragPrompt;

    //TODO: 오히려 이상하게 바뀔 때가 있어서 점검 필요
    // @Bean
    // QueryTransformer queryTransformer(ChatClient.Builder chatClientBuilder) {
    //     log.info("QueryTransformer 생성...");
    //     return RewriteQueryTransformer.builder()
    //             .chatClientBuilder(chatClientBuilder.defaultOptions(
    //                 ChatOptions.builder().temperature(0.0).build()
    //             ))
    //             .build();
    // }

    @Bean
    MultiQueryExpander queryExpander(ChatClient.Builder chatClientBuilder) {
        log.info("MultiQueryExpander 생성...");
        return MultiQueryExpander.builder()
                .chatClientBuilder(chatClientBuilder)
                .numberOfQueries(3)
                .build();
    }

    @Bean
    QueryAugmenter queryAugmenter() {
        log.info("ContextualQueryAugmenter 생성...");
        return ContextualQueryAugmenter.builder()
                .documentFormatter(this::formatDocuments)
                .promptTemplate(new PromptTemplate(ragPrompt))
                .allowEmptyContext(true)
                .build();
    }
    
    /**
     * 문서 리스트를 포맷팅하여 문자열로 변환합니다.
     * 각 문서에 대해 파일명, 페이지 번호(있는 경우), 정제된 내용을 포함합니다.
     * 
     * @param documents 포맷팅할 문서 리스트
     * @return 포맷팅된 문서 문자열
     */
    private String formatDocuments(List<Document> documents) {
        StringBuilder result = new StringBuilder();
        for (Document doc : documents) {
            Map<String, Object> metadata = doc.getMetadata();
            
            String fileName = Optional.ofNullable(metadata.get("file_name"))
                    .map(Object::toString)
                    .orElse("unknown_file");
            
            String pageNumber = Optional.ofNullable(metadata.get("page_number"))
                    .map(Object::toString)
                    .orElse(null);
          
           String cleanedText = Optional.ofNullable(doc.getText())
                    .map(text -> text.replaceAll("\\s+", " ").trim())
                    .orElse("");
            
            result.append("[file_name]: ").append(fileName).append("\n");
            
            if (pageNumber != null) {
                result.append("[page_number]: ").append(pageNumber).append("\n");
            }
            
            result.append("[content]: ").append(cleanedText).append("\n\n");
        }
        return result.toString().trim();
    }
    
}
