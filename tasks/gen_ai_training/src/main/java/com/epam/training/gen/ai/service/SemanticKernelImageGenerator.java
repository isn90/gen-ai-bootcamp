package com.epam.training.gen.ai.service;

import com.epam.training.gen.ai.config.OpenAIConfig;
import com.epam.training.gen.ai.model.RequestPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Slf4j
@Service
public class SemanticKernelImageGenerator {
    public static final String JSON_PATH_IMAGE_URL_AT_0_INDEX = "/choices/0/message/custom_content/attachments/0/url";
    public static final String JSON_PATH_IMAGE_URL_AT_1_INDEX = "/choices/0/message/custom_content/attachments/1/url";

    private final OpenAIConfig openAIConfig;
    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public String generateImage(RequestPayload payload) {
        var requestBody = new HashMap<>();
        requestBody.put("messages", new Object[]{Map.of("role", "user", "content", payload.getUserPrompt())});
        requestBody.put("max_tokens", 1000);

        var requestJson = objectMapper.writeValueAsString(requestBody);

        var request = HttpRequest.newBuilder()
                .uri(URI.create(openAIConfig.getEndpoint() + "/openai/deployments/" +
                         payload.getDeploymentModel() + "/chat/completions?api-version=2023-12-01-preview"))
                .header("Content-Type", "application/json")
                .header("Api-Key", openAIConfig.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            var jsonResponse = objectMapper.readTree(response.body());
            log.info("jsonResponse={}", jsonResponse);
            var imagePath = jsonResponse.at(JSON_PATH_IMAGE_URL_AT_0_INDEX).asText();
            return StringUtils.isEmpty(imagePath) ? jsonResponse.at(JSON_PATH_IMAGE_URL_AT_1_INDEX).asText(): imagePath ;
        } else {
            throw new RuntimeException("Failed to generate image: " + response.body());
        }
    }
}
