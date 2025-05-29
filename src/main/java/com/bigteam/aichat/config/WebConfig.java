package com.bigteam.aichat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**") // 모든 엔드포인트에 대해 CORS 설정 적용
				.allowedOriginPatterns("*") // 허용할 Origin
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
				.allowedHeaders("*") // 허용할 헤더
				.allowCredentials(true) // 쿠키와 같은 자격 증명 허용
				.maxAge(3600); // preflight 요청 캐시 시간 (초 단위)
	}
}
