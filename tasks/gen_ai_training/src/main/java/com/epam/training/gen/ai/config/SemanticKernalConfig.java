package com.epam.training.gen.ai.config;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.epam.training.gen.ai.plugins.*;
import com.epam.training.gen.ai.service.BookingService;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SemanticKernalConfig {

    @Bean
    public ChatCompletionService chatCompletionService(@Qualifier("genericOpenAiAsyncClient") OpenAIAsyncClient asyncClient, @Value("${client-openai-deployment-name}") String deploymentName) {
        return OpenAIChatCompletion.builder()
                .withOpenAIAsyncClient(asyncClient)
                .withModelId(deploymentName)
                .build();
    }

    @Bean
    public Kernel pluginBean(ChatCompletionService chatCompletionService) {
        // Import the LightsPlugin
        KernelPlugin lightPlugin = KernelPluginFactory.createFromObject(new LightsPlugin(),"LightsPlugin");
        KernelPlugin timePlugin = KernelPluginFactory.createFromObject(new TimePlugin(),"TimePlugin");
        KernelPlugin agePlugin = KernelPluginFactory.createFromObject(new AgeCalculatorPlugin(),"AgePlugin");
        KernelPlugin weatherPlugin = KernelPluginFactory.createFromObject(new WeatherForecastPlugin(),"WeatherPlugin");
        KernelPlugin bookTicketsPlugin = KernelPluginFactory.createFromObject(new BookTicketsPlugin(new BookingService()),"BookTicketsPlugin");

        // Create a kernel with Azure OpenAI chat completion and plugin
        return Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
                .withPlugin(lightPlugin)
                .withPlugin(timePlugin)
                .withPlugin(agePlugin)
                .withPlugin(weatherPlugin)
                .withPlugin(bookTicketsPlugin)
                .build();
    }
}
