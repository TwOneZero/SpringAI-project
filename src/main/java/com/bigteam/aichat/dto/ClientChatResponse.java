package com.bigteam.aichat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ClientChatResponse {
	String chatId;
	String response;
	Integer refLocation;
}
