package com.bigteam.aichat.monitor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitor")
@Tag(name = "Prometheus 메트릭", description = "Prometheus에서 모니터링 데이터를 조회하기 위한 API 엔드포인트")
public class PrometheusMetricController {

    private final PrometheusMetricService prometheusService;

    public PrometheusMetricController(PrometheusMetricService prometheusService) {
        this.prometheusService = prometheusService;
    }
    
    @GetMapping("/query")
    @Operation(
        summary = "즉시 쿼리 실행",
        description = "현재 시간에 Prometheus 즉시 쿼리를 실행합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "쿼리가 성공적으로 실행되었습니다"),
            @ApiResponse(responseCode = "400", description = "잘못된 쿼리 문법", content = @Content),
            @ApiResponse(responseCode = "422", description = "쿼리 실행 오류", content = @Content),
            @ApiResponse(responseCode = "503", description = "쿼리 타임아웃 또는 중단", content = @Content)
        }
    )
    public Mono<ResponseEntity<Map<String, Object>>> queryMetric(
            @Parameter(description = "Prometheus 쿼리 식", required = true, example = "up") 
            @RequestParam String query) {
        return prometheusService.queryMetric(query)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/query-range")
    @Operation(
        summary = "범위 쿼리 실행", 
        description = "지정된 시간 범위에 대해 Prometheus 쿼리를 실행합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "쿼리가 성공적으로 실행되었습니다"),
            @ApiResponse(responseCode = "400", description = "잘못된 쿼리 파라미터", content = @Content),
            @ApiResponse(responseCode = "422", description = "쿼리 실행 오류", content = @Content),
            @ApiResponse(responseCode = "503", description = "쿼리 타임아웃 또는 중단", content = @Content)
        }
    )
    public Mono<ResponseEntity<Map<String, Object>>> queryRangeMetric(
            @Parameter(description = "Prometheus 쿼리 식", required = true, example = "rate(http_server_requests_seconds_count[5m])") 
            @RequestParam String query,
            @Parameter(description = "Unix 초 단위 시작 타임스탬프", example = "1711084800") 
            @RequestParam(required = false) Long start,
            @Parameter(description = "Unix 초 단위 종료 타임스탬프", example = "1711171200") 
            @RequestParam(required = false) Long end,
            @Parameter(description = "쿼리 해상도 단계 간격", example = "15s") 
            @RequestParam(defaultValue = "15s") String step) {
        long endTime = end != null ? end : Instant.now().getEpochSecond();
        long startTime = start != null ? start : endTime - 3600; // 기본 1시간
        
        return prometheusService.queryRangeMetric(query, startTime, endTime, step)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/metrics/chat-client/response-time")
    @Operation(
        summary = "채팅 클라이언트 응답 시간 메트릭 조회",
        description = "Spring-AI 채팅 클라이언트의 응답 시간 메트릭을 반환합니다"
    )
    public Mono<ResponseEntity<Map<String, Object>>> getChatClientResponseTimeMetrics() {
        return prometheusService.getChatClientResponseTimeMetrics()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/metrics/chat-client/call-count")
    @Operation(
        summary = "채팅 클라이언트 호출 횟수 메트릭 조회",
        description = "Spring-AI 채팅 클라이언트의 호출 횟수 메트릭을 반환합니다"
    )
    public Mono<ResponseEntity<Map<String, Object>>> getChatClientCallCountMetrics() {
        return prometheusService.getChatClientCallCountMetrics()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/metrics/chat-model/response-time")
    @Operation(
        summary = "채팅 모델 응답 시간 메트릭 조회",
        description = "Spring-AI 채팅 모델의 응답 시간 메트릭을 반환합니다"
    )
    public Mono<ResponseEntity<Map<String, Object>>> getChatModelResponseTimeMetrics() {
        return prometheusService.getChatModelResponseTimeMetrics()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/metrics/token-usage")
    @Operation(
        summary = "토큰 사용량 메트릭 조회",
        description = "Spring-AI 채팅 모델의 토큰 사용량 메트릭을 반환합니다"
    )
    public Mono<ResponseEntity<Map<String, Object>>> getTokenUsageMetrics() {
        return prometheusService.getTokenUsageMetrics()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/metrics/vector-store/query-time")
    @Operation(
        summary = "벡터 스토어 쿼리 시간 메트릭 조회",
        description = "벡터 스토어 작업의 쿼리 시간 메트릭을 반환합니다"
    )
    public Mono<ResponseEntity<Map<String, Object>>> getVectorStoreQueryTimeMetrics() {
        return prometheusService.getVectorStoreQueryTimeMetrics()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/metrics/all")
    @Operation(
        summary = "모든 Spring-AI 메트릭 조회",
        description = "단일 응답으로 모든 사용 가능한 Spring-AI 메트릭을 반환합니다"
    )
    public Mono<ResponseEntity<Map<String, Object>>> getAllSpringAiMetrics() {
        return prometheusService.getAllSpringAiMetrics()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/metric-names")
    @Operation(
        summary = "사용 가능한 메트릭 이름 조회",
        description = "사용 가능한 모든 Prometheus 메트릭 이름의 목록을 반환합니다"
    )
    public Mono<ResponseEntity<List<String>>> getMetricNames() {
        return prometheusService.getMetricNames()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/custom-query")
    @Operation(
        summary = "사용자 지정 범위 쿼리 실행",
        description = "지정된 시간 범위와 단계로 사용자 지정 Prometheus 쿼리를 실행합니다",
        responses = {
            @ApiResponse(responseCode = "200", description = "쿼리가 성공적으로 실행되었습니다"),
            @ApiResponse(responseCode = "400", description = "잘못된 쿼리 파라미터", content = @Content),
            @ApiResponse(responseCode = "422", description = "쿼리 실행 오류", content = @Content),
            @ApiResponse(responseCode = "503", description = "쿼리 타임아웃 또는 중단", content = @Content)
        }
    )
    public Mono<ResponseEntity<Map<String, Object>>> customQuery(
            @Parameter(description = "Prometheus 쿼리 식", required = true, example = "sum(rate(http_server_requests_seconds_count[5m]))") 
            @RequestParam String query,
            @Parameter(description = "초 단위 시간 범위", example = "3600") 
            @RequestParam(defaultValue = "3600") int timeRange,
            @Parameter(description = "쿼리 해상도 단계 간격", example = "15s") 
            @RequestParam(defaultValue = "15s") String step) {

        long endTime = Instant.now().getEpochSecond();
        long startTime = endTime - timeRange;
        
        return prometheusService.queryRangeMetric(query, startTime, endTime, step)
                .map(ResponseEntity::ok);
    }
    
    /**
     * 서비스 상태 정보
     */
    @GetMapping("/metrics/service-status")
    @Operation(
        summary = "서비스 상태 메트릭 조회",
        description = "모델 이름, 버전 및 운영 상태를 포함한 서비스의 현재 상태 정보를 반환합니다"
    )
    public Mono<ResponseEntity<Map<String, Object>>> getServiceStatusMetrics() {
        return prometheusService.getServiceStatusMetrics()
                .map(ResponseEntity::ok);
    }
    
    /**
     * API 호출 성공률 (대시보드의 Trace Success Rate)
     */
    @GetMapping("/metrics/api-success-rate")
    @Operation(
        summary = "API 성공률 조회",
        description = "API 호출 성공률(대시보드의 Trace Success Rate)을 반환합니다"
    )
    public Mono<ResponseEntity<Map<String, Object>>> getApiTraceSuccessRate() {
        return prometheusService.getApiTraceSuccessRate()
                .map(ResponseEntity::ok);
    }
    
    /**
     * 기간별 사용자 통계
     */
    @GetMapping("/metrics/user-stats")
    @Operation(
        summary = "기간별 사용자 통계 조회",
        description = "지정된 기간(일간, 주간 또는 월간)에 따른 사용자 활동 통계를 반환합니다"
    )
    public Mono<ResponseEntity<Map<String, Object>>> getUserStatsByPeriod(
            @Parameter(description = "통계 시간 범위 (일간, 주간 또는 월간)", example = "daily") 
            @RequestParam(defaultValue = "daily") String period) {
        return prometheusService.getUserStatsByPeriod(period)
                .map(ResponseEntity::ok);
    }
    
    /**
     * LLM 응답 시간 (ms)
     */
    @GetMapping("/metrics/llm-response-time")
    @Operation(
        summary = "LLM 응답 시간 메트릭 조회",
        description = "밀리초 단위의 LLM 호출 응답 시간 메트릭을 반환합니다"
    )
    public Mono<ResponseEntity<Map<String, Object>>> getLlmResponseTimeMetrics() {
        return prometheusService.getLlmResponseTimeMetrics()
                .map(ResponseEntity::ok);
    }
    
    /**
     * 호출당 토큰 사용량
     */
    @GetMapping("/metrics/tokens-per-call")
    @Operation(
        summary = "호출당 토큰 사용량 메트릭 조회",
        description = "LLM 호출당 평균 토큰 사용량을 반환합니다"
    )
    public Mono<ResponseEntity<Map<String, Object>>> getTokensPerCallMetrics() {
        return prometheusService.getTokensPerCallMetrics()
                .map(ResponseEntity::ok);
    }
    
    /**
     * 대시보드 종합 메트릭 (단일 API 호출로 모든 대시보드 데이터 제공)
     */
    @GetMapping("/dashboard")
    @Operation(
        summary = "모든 대시보드 메트릭 조회",
        description = "단일 API 호출로 대시보드에 필요한 모든 메트릭을 반환합니다"
    )
    public Mono<ResponseEntity<Map<String, Object>>> getDashboardMetrics() {
        return prometheusService.getDashboardMetrics()
                .map(ResponseEntity::ok);
    }
}
