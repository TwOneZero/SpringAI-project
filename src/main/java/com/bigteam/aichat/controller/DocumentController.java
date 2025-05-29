package com.bigteam.aichat.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bigteam.aichat.entity.DocumentInfo;
import com.bigteam.aichat.service.DataLoaderService;
import com.bigteam.aichat.service.DocumentService;
import com.bigteam.aichat.service.VectorStoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/document")
@Tag(name = "DocumentController", description = "Document 관리 API")
public class DocumentController {

    private final DataLoaderService loaderService;
    private final DocumentService documentService;
    private final VectorStoreService vectorStoreService;

    @Operation(summary = "RAG 파일 업로드", description = "파일 업로드", tags = {"DocumentController"})
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "응답 생성 완료")})
	@PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<DocumentInfo> uploadRag(
			@RequestPart(value = "file", required = true) MultipartFile file,
			@RequestParam(value = "userId", required = true) String userId
		) throws IOException {
		DocumentInfo doc = loaderService.processAndStoreDocument(file, userId);
		return ResponseEntity.ok(doc);
	}

	@Operation(summary = "단일, 여러 문서 삭제", description = "문서 ID 목록으로 문서 일괄 삭제", tags = {"DocumentController"})
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "문서 삭제 완료")})
	@DeleteMapping(path = "documents", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> deleteDocuments(@RequestParam List<Long> documentIds) {
		if (documentIds == null || documentIds.isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("error", "문서 ID 목록이 비어있습니다."));
		}

		documentService.deleteDocuments(documentIds);
		vectorStoreService.removeDocumentsFromVectorStore(documentIds);
		return ResponseEntity.ok(Map.of("success", true, "message", "문서 삭제 완료"));
	}

	@Operation(summary = "단일, 여러 문서 활성화 토글", description = "여러 문서의 활성화 상태를 일괄 토글", tags = {"DocumentController"})
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "문서 활성화 상태 변경 완료")})
	@PutMapping(path = "documents/toggle", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<DocumentInfo>> toggleDocuments(
			@RequestParam(value = "documentIds", required = true) List<Long> documentIds,
			@RequestParam(value = "userId", required = true) String userId,
			@RequestParam(value = "isActive", required = true) boolean isActive
	) {
		if (documentIds == null || documentIds.isEmpty()) {
			return ResponseEntity.badRequest().build();
		}
		List<DocumentInfo> docs = documentService.toggleDocumentsActive(userId, documentIds, isActive);
		return ResponseEntity.ok(docs);
	}
}

