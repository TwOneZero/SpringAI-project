package com.bigteam.aichat.service;

import com.bigteam.aichat.dto.ClientChatRequest;
import com.bigteam.aichat.rag.EnhancedDocumentRagService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ai.chat.memory.ChatMemory;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatClient chatClient;
	private final ChatModel chatModel;
	private final EnhancedDocumentRagService enhancedDocumentRagService;

	@Value("classpath:prompts/rag-prompt.txt")
	private Resource ragPrompt;

	/**
	 * 채팅을 수행하고 응답을 반환
	 *
	 * @param request 클라이언트의 채팅 요청
	 * @return ChatResponse 스트림 (Flux)
	 * @throws IOException 입출력 예외 발생 시
	 */
	@Transactional
	public Flux<ChatResponse> chat(ClientChatRequest request) throws IOException {

		//기본 chatId
		String chatId = Optional
				.ofNullable(request.getChatId())
				.orElse(UUID.randomUUID().toString());

		// 모델 이름
		String model = request.getModel() == null ? chatModel.getDefaultOptions().getModel() : request.getModel();
		log.info("채팅 결과 생성 중...");
		ChatOptions chatOptions = ChatOptions.builder().model(model)
				.temperature(request.getTemperature())
				.maxTokens(request.getMaxTokens())
				.topP(request.getTopP())
				.topK(request.getTopK())
				.build();

		// 활성 문서 ID 검색
		List<Long> activeDocIds = enhancedDocumentRagService.getActiveDocFilter(chatId);
		// 활성 문서 ID가 없으면 기본 채팅 수행
		if (activeDocIds.isEmpty()) {
			log.info("활성 문서가 없습니다. 기본 채팅 수행 중...");
			return chatClient.prompt()
					.options(chatOptions)
					.user(request.getPrompt())
					.advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))
					.stream().chatResponse();
		}
		return chatClient.prompt()
				.options(chatOptions)
				.user(request.getPrompt())
				.advisors(enhancedDocumentRagService.getRagAdvisor())
				.advisors(advisor -> advisor
						.param(ChatMemory.CONVERSATION_ID, chatId)
						.param(VectorStoreDocumentRetriever.FILTER_EXPRESSION,
								"document_id in [" + activeDocIds.stream()
								.map(String::valueOf).collect(Collectors.joining(",")) + "]"
						)
				)
				.stream()
				.chatResponse()
				.doOnCancel(() -> log.info("연결 종료로 인해 스트림이 취소되었습니다. 종료 chatId: {}", chatId))			
				.doOnError(e -> log.error("오류 발생: {}", e.getMessage()))
				// IOException 발생 시 무시하고 빈 Flux 반환
				.onErrorResume(IOException.class, e -> {
					log.warn("클라이언트 연결 종료에 따른 IOException 무시: {}", e.getLocalizedMessage());
					return Flux.empty();
				});
	}
}
