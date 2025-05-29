package com.bigteam.aichat.repository;

import com.bigteam.aichat.entity.DocumentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DocumentInfoRepository extends JpaRepository<DocumentInfo, Long> {

	List<DocumentInfo> findByUserId(String userId);

	// 특정 chatId 에서 onChat 이 true 인 documentId 조회
	@Query("SELECT d.id FROM DocumentInfo d WHERE d.userId = :userId AND d.onChat = true")
	List<Long> findActiveDocumentIdsByUserId(@Param("userId") String userId);


	@Modifying
	@Transactional
	@Query("DELETE FROM DocumentInfo d WHERE d.id IN :ids")
	void deleteAllByIds(@Param("ids") List<Long> ids);
}