package com.bigteam.aichat.repository;


import com.bigteam.aichat.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
	/**
	 * 특정 사용자의 전체 대화 기록 조회
	 */
	List<ChatHistory> findByUserIdOrderByCreatedAtDesc(String userId);

	/**
	 * 특정 세션의 대화 기록 조회
	 */
	List<ChatHistory> findByChatIdOrderByCreatedAtAsc(String chatId);

	List<ChatHistory> findByChatId(String chatId);
}