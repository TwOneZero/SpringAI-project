package com.bigteam.aichat.service;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// TODO: 요약 프로세스 구현
@Slf4j
@RequiredArgsConstructor
@Service
public class SummaryService {
    private final ChatModel chatModel;

    public boolean isNeedSummary(String query) {
        return false;
    }

	public String SummaryDocument(String query) {
		// 핵심 단어 추출을 위한 프롬프트 구성

		ChatResponse response = chatModel.call(
				new Prompt("""
						
						""",
						ChatOptions.builder()
								   .model("gemma2:2b")
                                   .temperature(0.5)
								   .build()
				));
		String extracted = response.getResult().getOutput().getText();
		log.info("추출된 단어 :{}", extracted);

		return extracted;
	}

}