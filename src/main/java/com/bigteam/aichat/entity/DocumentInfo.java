package com.bigteam.aichat.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_info")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DocumentInfo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	//TODO: 릴레이션 필요
	@Column
	private String userId;

	@Column(nullable = false)
	private String filename;

	@Column(nullable = false)
	private String contentType;

	@Column(name = "file_size")
	private Long fileSize;

	@Column(name = "chat_id")
	private String chatId;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "on_chat")
	private boolean onChat; // 대화 중 업로드한 파일 정보

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}
}