package com.epam.training.gen.ai.controller;

import com.epam.training.gen.ai.model.RequestPayload;
import com.epam.training.gen.ai.service.RAGService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/rag/api/v1")
public class RAGController {

    private final RAGService ragService;

    @PostMapping(value = "/upload-resource", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> generateResponse(@RequestBody RequestPayload payload) throws Exception {
        var response = ragService.uploadAndStoreKnowledgeFromFile();
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/info/ask", produces = MediaType.TEXT_PLAIN_VALUE)
    public String ask(@RequestBody RequestPayload request) throws ExecutionException, InterruptedException {
        return ragService.getResponseFromRAG(request);
    }
}
