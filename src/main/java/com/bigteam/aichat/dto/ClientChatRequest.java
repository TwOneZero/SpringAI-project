package com.bigteam.aichat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Chat request parameters")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ClientChatRequest {
	@Schema(description = "User ID", example = "1")
	private String userId;                  // 유저 ID
	@Schema(description = "Chat session ID", example = "12345")
	private String chatId;                  // 세션 ID
	@Schema(description = "User input message", example = "")
	private String prompt;                  // 사용자 입력
	@Schema(description = "AI model name", example = "bigteam-gemma3")
	private String model;                   // 모델 선택
	@Schema(description = "Response randomness (0.0 to 1.0)", defaultValue = "0.5")
	private Double temperature = 0.8;             // 응답 다양성 조절
	@Schema(description = "Maximum number of tokens in response", defaultValue = "8196")
	private Integer maxTokens = 8196;              // 응답 길이 제한
	@Schema(description = "Top-p sampling parameter", defaultValue = "0.90")
	private Double topP = 0.90;                    // 샘플링 조절
	@Schema(description = "Top-k sampling parameter", defaultValue = "30")
	private Integer topK = 30;                    // 샘플링 조절
}
