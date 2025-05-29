package com.bigteam.aichat.monitor;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class MetricErrorHandler {

    /**
     * Map 형태의 응답을 반환하는 메트릭 쿼리에 대한 에러 핸들러
     * 
     * @param operation 작업 설명
     * @param params 로깅할 파라미터 정보
     * @return 에러 발생 시 에러 정보를 담은 Map을 반환하는 함수
     */
    public static Function<Throwable, Mono<Map<String, Object>>> handleMetricError(String operation, Map<String, Object> params) {
        return e -> {
            log.error("Error in {}: {}, params: {}", operation, e.getMessage(), params, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("operation", operation);
            errorResponse.putAll(params);
            return Mono.just(errorResponse);
        };
    }

    /**
     * 간단한 파라미터로 에러 핸들러 생성
     */
    public static Function<Throwable, Mono<Map<String, Object>>> handleMetricError(String operation, String paramName, Object paramValue) {
        Map<String, Object> params = new HashMap<>();
        params.put(paramName, paramValue);
        return handleMetricError(operation, params);
    }

    /**
     * 파라미터 없이 에러 핸들러 생성
     */
    public static Function<Throwable, Mono<Map<String, Object>>> handleMetricError(String operation) {
        return handleMetricError(operation, new HashMap<>());
    }
}