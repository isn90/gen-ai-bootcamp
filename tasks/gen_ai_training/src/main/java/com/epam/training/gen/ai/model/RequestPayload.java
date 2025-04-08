package com.epam.training.gen.ai.model;

import lombok.Data;

@Data
public class RequestPayload {
    private String userPrompt;
    private String deploymentName;
    private String deploymentModel;
    private Double temperature;
    private Integer maxTokens;
}
