package com.epam.training.gen.ai.service;

import java.util.List;

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
    

    public PromptService(OpenAIAsyncClient aiAsyncClient, @Value("${client-openai-deployment-name}") String deploymentName) {
		this.aiAsyncClient = aiAsyncClient;
		this.deploymentName = deploymentName;
	}

	public List<String> getChatCompletions(String prompt) {
		log.info(prompt);
		var completions = aiAsyncClient
                .getChatCompletions(deploymentName, new ChatCompletionsOptions(List.of(new ChatRequestUserMessage(prompt))))
                .block();
		log.info("" + completions);
        
		var responses = completions.getChoices().stream()
                .map(comp -> comp.getMessage().getContent())
                .toList();
        log.info(responses.toString());
        return responses;
    }
}
