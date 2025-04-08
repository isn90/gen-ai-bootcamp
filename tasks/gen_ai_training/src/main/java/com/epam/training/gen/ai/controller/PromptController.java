package com.epam.training.gen.ai.controller;

import java.util.HashMap;
import java.util.Map;

import com.epam.training.gen.ai.model.RequestPayload;
import com.epam.training.gen.ai.service.SemanticKernelImageGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.epam.training.gen.ai.service.PromptService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PromptController {

    private final PromptService promptService;
    //private final SemanticKernelImageGenerator imageGenerator;

    @PostMapping(value = "/generate-response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> generateResponse(@RequestBody RequestPayload payload) {
        Map<String, Object> response = new HashMap<>();

        try {
            var chats = promptService.getChatCompletions(payload);
            response.put("status", "success");
            response.put("data", chats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error generating response");
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
        }
    }

    /*@PostMapping(value = "/generate-image", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String generateImage(@RequestBody RequestPayload payload) {
        return imageGenerator.generateImage(payload);
    }*/
}
