package com.bigteam.aichat.repository;


import com.bigteam.aichat.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {

	/**
	 * 특정 사용자의 모든 템플릿 조회
	 */
	List<PromptTemplate> findByUserId(String userId);

	/**
	 * 특정 사용자와 이름에 해당하는 템플릿 조회
	 */
	Optional<PromptTemplate> findByUserIdAndName(String userId, String name);

	/**
	 * 특정 사용자의 템플릿 삭제
	 */
	void deleteByUserIdAndName(String userId, String name);
}
