package com.bigteam.aichat.config;


import lombok.RequiredArgsConstructor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class ChatConfig {

	@Value("classpath:prompts/persona.txt")
	private Resource persona;

	private final JdbcChatMemoryRepository chatMemoryRepository;

	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder
				.defaultSystem(persona, StandardCharsets.UTF_8)
				.defaultAdvisors(
						MessageChatMemoryAdvisor.builder(chatMemory()).build(),
						new SimpleLoggerAdvisor())
				.build();
	}

	@Bean
	ChatMemory chatMemory() {
		return MessageWindowChatMemory.builder()
				.chatMemoryRepository(chatMemoryRepository)
				.maxMessages(20)
				.build();
	}
}
