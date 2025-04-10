package com.epam.training.gen.ai.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.ai.openai.models.*;
import com.epam.training.gen.ai.config.DeploymentModelConfig;
import com.epam.training.gen.ai.model.RequestPayload;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.responseformat.ResponseFormat;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.azure.ai.openai.OpenAIAsyncClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PromptService {

	private final OpenAIAsyncClient aiAsyncClient;
	private final OpenAIAsyncClient imageGenerationAiAsyncClient;
	private final String deploymentName;
	private final ChatHistory chatHistory;
	private final ChatCompletionService chatCompletionService;
	private final Kernel kernel;
	private final Map<String, String> textModel;
	private final Map<String, String> imageModel;


	public PromptService(@Qualifier("genericOpenAiAsyncClient") OpenAIAsyncClient aiAsyncClient, @Qualifier("imageGenerationOpenAiAsyncClient") OpenAIAsyncClient imageGenerationAiAsyncClient,
						 @Value("${client-openai-deployment-name}") String deploymentName, ChatHistory chatHistory, ChatCompletionService chatCompletionService, Kernel kernel,
						 DeploymentModelConfig modelConfig, Map<String, String> textModel, Map<String, String> imageModel) {
		this.aiAsyncClient = aiAsyncClient;
        this.imageGenerationAiAsyncClient = imageGenerationAiAsyncClient;
        this.deploymentName = deploymentName;
		this.chatHistory = chatHistory;
		this.chatCompletionService = chatCompletionService;
		this.kernel = kernel;
		this.textModel = modelConfig.getTextModel();
		this.imageModel = modelConfig.getImageModel();
    }

	public List<String> getPromptResponse(RequestPayload payload) {
		log.info("payload={}", payload);
		String deploymentName = "";
		String deploymentModel = payload.getDeploymentModel();
		log.info("deploymentModel={}", deploymentModel);
		chatHistory.addUserMessage(payload.getUserPrompt());

		log.info("imageModel={}, textModel={}", imageModel, textModel);

		if(imageModel.containsKey(deploymentModel)){
			deploymentName = imageModel.get(deploymentModel);
			return generateImage(payload.getUserPrompt(), deploymentName);
		} else {
			deploymentName = textModel.get(deploymentModel);
			log.info("deploymentName={}", deploymentName);
			return generateChatResponse(payload, deploymentName);
		}
	}

	private List<String> generateChatResponse(RequestPayload payload, String deploymentName) {
		List<String> chats = new ArrayList<>();
		log.info("Building chat config deploymentName={}", deploymentName);
		ChatCompletionService chatCompletionService = buildChatCompletionService(deploymentName);
		log.info("chatCompletionService={}", chatCompletionService);

		//Prompt Execution Settings Initialization
		InvocationContext context = buildInvocationContext(payload, deploymentName);
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

	public List<String> generateImage(String prompt, String model) {

		ImageGenerationOptions options = new ImageGenerationOptions(prompt)
				.setN(1) // Number of images to generate
				.setQuality(ImageGenerationQuality.HD)
				.setSize(ImageSize.SIZE1024X1024); // Image resolution

		ImageGenerations imageGenerations = imageGenerationAiAsyncClient.getImageGenerations(model, options)
				.doOnNext(res -> System.out.println("ImageGenerations API Response: " + res))
				.doOnError(err -> System.err.println("Error occurred: " + err.getMessage()))
				.block();

        assert imageGenerations != null;
        List<String> urls = imageGenerations.getData().stream()
				.map(ImageGenerationData::getUrl)
				.collect(Collectors.toList());
		log.info("urls={}", urls);
		return urls;
	}

	private InvocationContext buildInvocationContext(RequestPayload payload, String deploymentName) {
		return InvocationContext.builder()
				.withPromptExecutionSettings(PromptExecutionSettings.builder()
						.withModelId(deploymentName)
						.withTemperature(payload.getTemperature())
						.withMaxTokens(payload.getMaxTokens())
						.withResponseFormat(ResponseFormat.Type.TEXT)
						.build())
				.build();
	}

	private ChatCompletionService buildChatCompletionService(String model) {
		return OpenAIChatCompletion.builder()
				.withOpenAIAsyncClient(aiAsyncClient)
				.withModelId(model)
				.build();
	}
}
