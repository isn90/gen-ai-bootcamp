package com.epam.training.gen.ai.config;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class OpenAIConfig {

    @Value("${client-openai-key}")
    private String apiKey;

    @Value("${client-openai-endpoint}")
    private String endpoint;

    @Bean//("genericOpenAiAsyncClient")
    public OpenAIAsyncClient openAIAsyncClient() {
        return new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(apiKey))
                .endpoint(endpoint)
                .buildAsyncClient();
    }

    /*@Bean("imageGenerationOpenAiAsyncClient")
    public OpenAIAsyncClient openAIAsyncClientBeanForImageGeneration(@Value("${azure-openai-imageGenerationKey}") String key, @Value("${azure-openai-imageGenerationEndpoint}") String endpoint) {
        return new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(key))
                .endpoint(endpoint)
                .buildAsyncClient();
    }*/

    @Bean
    public ChatHistory chatHistory() {
        return new ChatHistory();
    }

}
