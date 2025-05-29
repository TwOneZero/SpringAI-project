package com.bigteam.aichat.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String userId; // 사용자 ID

	@Column(nullable = false)
	private String chatId; // 세션 ID

	@Column(columnDefinition = "TEXT")
	private String prompt; // 사용자 입력 (null 가능: 파일 업로드만 한 경우)

	@Column(columnDefinition = "TEXT")
	private String response; // AI 응답 (null 가능: 파일 업로드만 한 경우)

	@Column(nullable = false)
	private String model; // 사용한 AI 모델명

	@Column(nullable = false)
	private LocalDateTime createdAt; // 대화 생성 시간

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "document_id", referencedColumnName = "id")
	private DocumentInfo documentInfo; // 업로드된 파일 (없을 수도 있음)

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

}
