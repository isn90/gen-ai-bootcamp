package com.epam.training.gen.ai.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.ai.openai.models.*;
import com.epam.training.gen.ai.config.DeploymentModelConfig;
import com.epam.training.gen.ai.model.LightModel;
import com.epam.training.gen.ai.model.RequestPayload;
import com.epam.training.gen.ai.plugins.AgeCalculatorPlugin;
import com.epam.training.gen.ai.plugins.BookTicketsPlugin;
import com.epam.training.gen.ai.plugins.TimePlugin;
import com.epam.training.gen.ai.plugins.WeatherForecastPlugin;
import com.google.gson.Gson;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
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


	public String getPromptResponse(RequestPayload payload) {
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

	private String generateChatResponse(RequestPayload payload, String deploymentName) {
		// Add a converter to the kernel to show it how to serialise LightModel objects into a prompt
		ContextVariableTypes
				.addGlobalConverter(
						ContextVariableTypeConverter.builder(LightModel.class)
								.toPromptString(new Gson()::toJson)
								.build());

		ContextVariableTypes
				.addGlobalConverter(
						ContextVariableTypeConverter.builder(TimePlugin.class)
								.toPromptString(new Gson()::toJson)
								.build());

		ContextVariableTypes
				.addGlobalConverter(
						ContextVariableTypeConverter.builder(AgeCalculatorPlugin.class)
								.toPromptString(new Gson()::toJson)
								.build());

		ContextVariableTypes
				.addGlobalConverter(
						ContextVariableTypeConverter.builder(WeatherForecastPlugin.class)
								.toPromptString(new Gson()::toJson)
								.build());

		ContextVariableTypes
				.addGlobalConverter(
						ContextVariableTypeConverter.builder(BookTicketsPlugin.class)
								.toPromptString(new Gson()::toJson)
								.build());
		//Prompt Execution Settings Initialization
		InvocationContext context = buildInvocationContext(payload, deploymentName);
		log.info("context={}", context);

		// Create a history to store the conversation
		chatHistory.addUserMessage(payload.getUserPrompt());

		List<ChatMessageContent<?>> results = chatCompletionService
				.getChatMessageContentsAsync(chatHistory, kernel, context)
				.block();

		assert results != null;
		System.out.println("Assistant > " + results.get(0));
		chatHistory.addSystemMessage(results.stream()
				.map(ChatMessageContent::getContent)
				.collect(Collectors.joining(" ")));
		return chatHistory.getMessages().stream()
				.map(chatMessageContent -> chatMessageContent.getAuthorRole().name()
						+ " : " + chatMessageContent.getContent())
				.collect(Collectors.joining("\n\n"));
	}

	public String generateImage(String prompt, String model) {

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
		return urls.get(0);
	}

	private InvocationContext buildInvocationContext(RequestPayload payload, String deploymentName) {
		return InvocationContext.builder()
				.withPromptExecutionSettings(PromptExecutionSettings.builder()
						.withModelId(deploymentName)
						.withTemperature(payload.getTemperature())
						.withMaxTokens(payload.getMaxTokens())
						.withResponseFormat(ResponseFormat.Type.TEXT)
						.build())
				.withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
				.build();
	}

	private ChatCompletionService buildChatCompletionService(String model) {
		return OpenAIChatCompletion.builder()
				.withOpenAIAsyncClient(aiAsyncClient)
				.withModelId(model)
				.build();
	}
}
