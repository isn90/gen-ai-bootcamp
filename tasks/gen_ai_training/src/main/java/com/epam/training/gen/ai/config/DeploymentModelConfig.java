package com.epam.training.gen.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "client-openai-deployments")
public class DeploymentModelConfig {
    private Map<String, String> textModel = new HashMap<>();
    private Map<String, String> imageModel = new HashMap<>();
}
