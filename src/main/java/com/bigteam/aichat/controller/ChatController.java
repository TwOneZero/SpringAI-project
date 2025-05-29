package com.bigteam.aichat.controller;

import com.bigteam.aichat.dto.ClientChatRequest;
import com.bigteam.aichat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chat")
@Tag(name = "ChatController", description = "LLM 채팅 API")
public class ChatController {

	private final ChatService chatService;

	@Operation(summary = "일반 채팅 응답 생성", description = "일반 채팅 응답 생성, 실시간 X", tags = {"ChatController"})
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "응답 생성 완료")})
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public Flux<String> chat(
			@RequestBody ClientChatRequest request
	) throws IOException {
		// return chatService.chat(request);
		return chatService.chat(request).flatMap(chat -> Flux.fromIterable(chat.getResults()))
						  .map(Generation::getOutput)
						  .map(AbstractMessage::getText);
	}

}
