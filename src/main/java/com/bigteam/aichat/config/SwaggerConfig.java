package com.bigteam.aichat.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String API_NAME = "Spring AI Chat App";
    private static final String API_VERSION = "v0.1";
    private static final String API_DESCRIPTION = "Spring AI 로 구현하는 RAG Chat Agent";

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info().title(API_NAME)
                                .description(API_DESCRIPTION)
                                .version(API_VERSION)
                                .contact(new Contact().name("Bigteam").url("http://www.bigteam.co.kr/"))
                                .license(new License().name("Apache 2.0").url(
                                        "https://www.apache.org/licenses/LICENSE-2.0"))
                );
    }
}
