package com.bigteam.aichat.monitor;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PrometheusMetricService {

    private final WebClient webClient;
    
    // @Value("${prometheus.url:http://localhost:9090}")
    private String prometheusUrl = "http://localhost:9090";

    public PrometheusMetricService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(prometheusUrl).build();
    }

    /**
     * 단일 시점의 메트릭 쿼리
     */
    public Mono<Map<String, Object>> queryMetric(String query) {
        return webClient.post()
                .uri("/api/v1/query")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("query", query))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .onErrorResume(MetricErrorHandler.handleMetricError("queryMetric", "query", query));
    }

    /**
     * 시간 범위에 대한 메트릭 쿼리
     */
    public Mono<Map<String, Object>> queryRangeMetric(String query, long startTime, long endTime, String step) {
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        params.put("step", step);

        return webClient.post()
                .uri("/api/v1/query_range")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("query", query)
                        .with("start", String.valueOf(startTime))
                        .with("end", String.valueOf(endTime))
                        .with("step", step))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .onErrorResume(MetricErrorHandler.handleMetricError("queryRangeMetric", params));
    }

    /**
     * 최근 1시간 동안의 데이터 쿼리 (기본값)
     */
    public Mono<Map<String, Object>> queryLastHour(String query) {
        long endTime = Instant.now().getEpochSecond();
        long startTime = endTime - 3600; // 1시간 (초 단위)
        return queryRangeMetric(query, startTime, endTime, "15s");
    }

    /**
     * 최근 24시간 동안의 데이터 쿼리
     */
    public Mono<Map<String, Object>> queryLast24Hours(String query) {
        long endTime = Instant.now().getEpochSecond();
        long startTime = endTime - 86400; // 24시간 (초 단위)
        return queryRangeMetric(query, startTime, endTime, "5m");
    }

    /**
     * Spring-AI ChatClient 호출 응답 시간
     */
    public Mono<Map<String, Object>> getChatClientResponseTimeMetrics() {
        // 실제 존재하는 메트릭 사용
        String query = "rate(gen_ai_client_operation_seconds_sum[5m]) / " +
                "rate(gen_ai_client_operation_seconds_count[5m])";
        return queryLastHour(query);
    }

    /**
     * Spring-AI ChatClient 호출 수
     */
    public Mono<Map<String, Object>> getChatClientCallCountMetrics() {
        String query = "sum(increase(gen_ai_client_operation_seconds_count[5m]))";
        return queryLastHour(query);
    }

    /**
     * Spring-AI ChatModel 호출 응답 시간
     */
    public Mono<Map<String, Object>> getChatModelResponseTimeMetrics() {
        String query = "rate(gen_ai_client_operation_seconds_sum[5m]) / rate(gen_ai_client_operation_seconds_count[5m])";
        return queryLastHour(query);
    }

    /**
     * Spring-AI ChatModel 토큰 사용량
     */
    public Mono<Map<String, Object>> getTokenUsageMetrics() {
        String query = "sum(increase(gen_ai_client_token_usage_total[5m]))";
        return queryLastHour(query);
    }

    /**
     * Spring-AI 벡터 스토어 쿼리 시간 (수정된 쿼리)
     * 메트릭 미존재로 인해 다른 성능 관련 메트릭으로 대체
     */
    public Mono<Map<String, Object>> getVectorStoreQueryTimeMetrics() {
        // 실제 벡터 스토어 관련 메트릭이 없으므로 HTTP 요청 시간으로 대체
        String query = "rate(http_server_requests_seconds_sum[5m]) / " +
                "rate(http_server_requests_seconds_count[5m])";
        return queryLastHour(query);
    }

    /**
     * 모든 주요 Spring-AI 메트릭 가져오기
     */
    public Mono<Map<String, Object>> getAllSpringAiMetrics() {
        Map<String, Object> allMetrics = new HashMap<>();
        
        return Mono.zip(
                getChatClientResponseTimeMetrics(),
                getChatClientCallCountMetrics(),
                getChatModelResponseTimeMetrics(),
                getTokenUsageMetrics(),
                getVectorStoreQueryTimeMetrics()
        ).map(tuple -> {
            allMetrics.put("chatClientResponseTime", tuple.getT1());
            allMetrics.put("chatClientCallCount", tuple.getT2());
            allMetrics.put("chatModelResponseTime", tuple.getT3());
            allMetrics.put("tokenUsage", tuple.getT4());
            allMetrics.put("vectorStoreQueryTime", tuple.getT5());
            return allMetrics;
        }).onErrorResume(MetricErrorHandler.handleMetricError("getAllSpringAiMetrics"));
    }

    /**
     * 사용 가능한 메트릭 이름 목록 가져오기
     */
    public Mono<List<String>> getMetricNames() {
        return webClient.get()
                .uri("/api/v1/label/__name__/values")
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (List<String>) ((Map) response.get("data")).get("result"))
                .onErrorResume(e -> {
                    log.error("Error getting metric names: {}", e.getMessage(), e);
                    return Mono.error(e); // 리스트 형태로 반환하기 어려우므로 에러를 전파
                });
    }
    
    /**
     * 서비스 상태 정보 가져오기
     */
    public Mono<Map<String, Object>> getServiceStatusMetrics() {
        Map<String, Object> statusInfo = new HashMap<>();
        
        // 기본값 설정 (실제 쿼리로 얻을 수 없는 경우 사용)
        statusInfo.put("modelName", "GPT-4o");
        statusInfo.put("version", "1.0.0");
        statusInfo.put("lastUpdate", "2025/03/28");
        statusInfo.put("serviceStatus", "운영 중");
        
        // up 메트릭을 사용하여 서비스 상태 확인
        return queryMetric("up")
                .map(response -> {
                    try {
                        List<Map<String, Object>> result = (List<Map<String, Object>>) ((Map<String, Object>) response.get("data")).get("result");
                        
                        if (result != null && !result.isEmpty()) {
                            Map<String, Object> upMetric = result.get(0);
                            List<Object> value = (List<Object>) upMetric.get("value");
                            String metricValue = value.get(1).toString();
                            statusInfo.put("serviceStatus", "1".equals(metricValue) ? "운영 중" : "중단됨");
                        }
                    } catch (Exception e) {
                        log.error("Error parsing service status: {}", e.getMessage());
                    }
                    
                    return statusInfo;
                })
                .onErrorResume(MetricErrorHandler.handleMetricError("getServiceStatus"));
    }

    /**
     * API 호출 성공률 지표 (Trace Success Rate)
     */
    public Mono<Map<String, Object>> getApiTraceSuccessRate() {
        // 원래 메트릭이 없으므로 HTTP 요청의 2xx 응답률로 대체
        String query = "sum(rate(http_server_requests_seconds_count{outcome=\"SUCCESS\"}[24h])) / " +
                      "sum(rate(http_server_requests_seconds_count[24h])) * 100";
        
        return queryMetric(query)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("timeRange", "24h"); // 지난 24시간 데이터

                    try {
                        List<Map<String, Object>> data = (List<Map<String, Object>>) 
                            ((Map<String, Object>) response.get("data")).get("result");
                        
                        if (data != null && !data.isEmpty()) {
                            Map<String, Object> successMetric = data.get(0);
                            List<Object> value = (List<Object>) successMetric.get("value");
                            String metricValue = value.get(1).toString();
                            double successRate = Double.parseDouble(metricValue);
                            result.put("successRate", String.format("%.2f", successRate));
                            result.put("rawData", data);
                        } else {
                            result.put("successRate", "00.00"); // 기본값 (대시보드에 표시된 값)
                        }
                    } catch (Exception e) {
                        log.error("Error parsing API success rate: {}", e.getMessage());
                        result.put("successRate", "00.00"); // 오류시 기본값
                    }
                    
                    return result;
                })
                .onErrorResume(MetricErrorHandler.handleMetricError("getApiTraceSuccessRate"));
    }

    /**
     * 기간별 사용자 통계 (일간/주간/월간)
     */
    public Mono<Map<String, Object>> getUserStatsByPeriod(String period) {
        String query;
        String step;
        long endTime = Instant.now().getEpochSecond();
        long startTime;
        
        switch (period.toLowerCase()) {
            case "weekly":
                // 주간 데이터: 지난 7일, 일별 집계
                startTime = endTime - 604800; // 7일
                step = "1d";       // 일 단위로 데이터 포인트 표시
                // 일별 집계 쿼리 - HTTP 요청 수 또는 AI 클라이언트 호출 수 
                query = "sum(increase(http_server_requests_seconds_count[1d])) by (instance)";
                break;
            case "monthly":
                // 월간 데이터: 지난 30일, 일별 집계
                startTime = endTime - 2592000; // 30일
                step = "1d";       // 일 단위로 데이터 포인트 표시
                // 일별 집계 쿼리
                query = "sum(increase(http_server_requests_seconds_count[1d])) by (instance)";
                break;
            case "daily":
            default:
                // 일간 데이터: 지난 24시간, 시간별 집계
                startTime = endTime - 86400; // 24시간
                step = "1h";       // 시간 단위로 데이터 포인트 표시
                // 시간별 집계 쿼리
                query = "sum(increase(http_server_requests_seconds_count[1h])) by (instance)";
        }
        
        // 해당 기간에 맞는 gen_ai_client_operation_seconds_count 메트릭이 있다면 더 정확한 데이터 가져오기
        // 메트릭이 있는지 확인하기 위한 코드 로직 필요시 추가 가능
        
        Map<String, Object> params = new HashMap<>();
        params.put("period", period);
        params.put("query", query);
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        params.put("step", step);
        
        return queryRangeMetric(query, startTime, endTime, step)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("period", period);
                    result.put("data", response);
                    return result;
                })
                .onErrorResume(MetricErrorHandler.handleMetricError("getUserStatsByPeriod", params));
    }
    
    /**
     * LLM 호출 응답 시간 (ms)
     */
    public Mono<Map<String, Object>> getLlmResponseTimeMetrics() {
        // LLM Call Response Time 쿼리 (대시보드에 표시된 1038ms에 대응)
        String query = "rate(gen_ai_client_operation_seconds_sum[1h]) / rate(gen_ai_client_operation_seconds_count[1h])";
        
        return queryMetric(query)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();

                    try {
                        List<Map<String, Object>> data = (List<Map<String, Object>>) 
                            ((Map<String, Object>) response.get("data")).get("result");
                        
                        if (data != null && !data.isEmpty()) {
                            Map<String, Object> metric = data.get(0);
                            List<Object> value = (List<Object>) metric.get("value");
                            String metricValue = value.get(1).toString();
                            double responseTimeSec = Double.parseDouble(metricValue);
                            int responseTimeMs = (int)(responseTimeSec * 1000); // 초를 밀리초로 변환
                            
                            result.put("responseTimeMs", responseTimeMs);
                            result.put("rawData", data);
                        } else {
                            result.put("responseTimeMs", 0); // 기본값 (대시보드에 표시된 값)
                        }
                    } catch (Exception e) {
                        log.error("Error parsing response time: {}", e.getMessage());
                        result.put("responseTimeMs", 0); // 오류시 기본값
                    }
                    
                    return result;
                })
                .onErrorResume(MetricErrorHandler.handleMetricError("getLlmResponseTimeMetrics"));
    }
    
    /**
     * 호출당 토큰 사용량
     */
    public Mono<Map<String, Object>> getTokensPerCallMetrics() {
        // 토큰 사용량 / 호출 수 계산 쿼리
        return Mono.zip(
            queryMetric("sum(increase(gen_ai_client_token_usage_total[1d]))"),
            queryMetric("sum(increase(gen_ai_client_operation_seconds_count[1d]))")
        ).map(tuple -> {
            Map<String, Object> result = new HashMap<>();
            
            try {
                // 총 토큰 수 처리
                Map<String, Object> tokenResponse = tuple.getT1();
                List<Map<String, Object>> tokenData = (List<Map<String, Object>>) 
                    ((Map<String, Object>) tokenResponse.get("data")).get("result");
                
                // 총 호출 수 처리
                Map<String, Object> callsResponse = tuple.getT2();
                List<Map<String, Object>> callsData = (List<Map<String, Object>>) 
                    ((Map<String, Object>) callsResponse.get("data")).get("result");
                
                double tokensPerCall = 0; // 기본값 (대시보드에 표시된 값)
                
                if (tokenData != null && !tokenData.isEmpty() && callsData != null && !callsData.isEmpty()) {
                    Map<String, Object> tokenMetric = tokenData.get(0);
                    Map<String, Object> callsMetric = callsData.get(0);
                    
                    List<Object> tokenValue = (List<Object>) tokenMetric.get("value");
                    List<Object> callsValue = (List<Object>) callsMetric.get("value");
                    
                    double totalTokens = Double.parseDouble(tokenValue.get(1).toString());
                    double totalCalls = Double.parseDouble(callsValue.get(1).toString());
                    
                    if (totalCalls > 0) {
                        tokensPerCall = totalTokens / totalCalls;
                    }
                }
                
                result.put("tokensPerCall", tokensPerCall);
                result.put("rawTokenData", tokenData);
                result.put("rawCallsData", callsData);
            } catch (Exception e) {
                log.error("Error calculating tokens per call: {}", e.getMessage());
                result.put("tokensPerCall", 0); // 오류시 기본값
            }
            
            return result;
        })
        .onErrorResume(MetricErrorHandler.handleMetricError("getTokensPerCallMetrics"));
    }

    /**
     * 대시보드용 종합 메트릭 가져오기
     */
    public Mono<Map<String, Object>> getDashboardMetrics() {
        Map<String, Object> dashboardMetrics = new HashMap<>();
        
        return Mono.zip(
                getServiceStatusMetrics(),
                getApiTraceSuccessRate(),
                getUserStatsByPeriod("daily"),
                getLlmResponseTimeMetrics(),
                getTokensPerCallMetrics()
        ).map(tuple -> {
            dashboardMetrics.put("serviceStatus", tuple.getT1());
            dashboardMetrics.put("apiTraceSuccess", tuple.getT2());
            dashboardMetrics.put("userStats", tuple.getT3());
            dashboardMetrics.put("responseTime", tuple.getT4());
            dashboardMetrics.put("tokensPerCall", tuple.getT5());
            return dashboardMetrics;
        }).onErrorResume(MetricErrorHandler.handleMetricError("getDashboardMetrics"));
    }
}