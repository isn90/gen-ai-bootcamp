package com.epam.training.gen.ai.model;

import lombok.Data;

@Data
public class RequestPayload {
    private String userPrompt;
    private Double temperature;
    private Integer maxTokens;
}
