package com.epam.training.gen.ai.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.epam.training.gen.ai.model.RequestPayload;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.responseformat.ResponseFormat;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestUserMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PromptService {

	private final OpenAIAsyncClient aiAsyncClient;
	private final String deploymentName;
	private final ChatHistory chatHistory;
	private final ChatCompletionService chatCompletionService;
	private final Kernel kernel;


	public PromptService(OpenAIAsyncClient aiAsyncClient, @Value("${client-openai-deployment-name}") String deploymentName, ChatHistory chatHistory, ChatCompletionService chatCompletionService, Kernel kernel) {
		this.aiAsyncClient = aiAsyncClient;
		this.deploymentName = deploymentName;
		this.chatHistory = chatHistory;
		this.chatCompletionService = chatCompletionService;
		this.kernel = kernel;
	}

	public List<String> getChatCompletions(RequestPayload payload) {
		log.info("payload={}", payload);
		List<String> chats = new ArrayList<>();
		chatHistory.addUserMessage(payload.getUserPrompt());

		//Prompt Execution Settings Initialization
		InvocationContext context = buildInvocationContext(payload);
		log.info("context={}", context);

		List<ChatMessageContent<?>> messageResponse = chatCompletionService.getChatMessageContentsAsync(chatHistory, kernel, context)
				.onErrorMap(ex -> new Exception(ex.getMessage())).block();
		log.info("messageResponse={}", messageResponse);

		if(messageResponse != null) {
			//adding to system messages in chatHistory
			chatHistory.addSystemMessage(messageResponse.stream()
					.map(ChatMessageContent::getContent)
					.collect(Collectors.joining()));
			log.info("chatHistory messages={}", chatHistory.getMessages());

			chats = messageResponse.stream()
					.map(ChatMessageContent::getContent)
					.toList();
		}
		log.info("chats={}", chats);
		return chats;
	}

	private InvocationContext buildInvocationContext(RequestPayload payload) {
		return InvocationContext.builder()
				.withPromptExecutionSettings(PromptExecutionSettings.builder()
						.withModelId(deploymentName)
						.withTemperature(payload.getTemperature())
						.withMaxTokens(payload.getMaxTokens())
						.withResponseFormat(ResponseFormat.Type.TEXT)
						.build())
				.build();
	}
}
