package com.bigteam.aichat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prompt_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptTemplate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String userId; // 사용자 ID (개인화)

	@Column(nullable = false, unique = true)
	private String name; // 템플릿 이름

	@Column(nullable = false, columnDefinition = "TEXT")
	private String template; // 프롬프트 템플릿 내용
}
