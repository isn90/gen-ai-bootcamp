package com.epam.training.gen.ai.controller;

import com.azure.ai.openai.models.EmbeddingItem;
import com.epam.training.gen.ai.service.TextEmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/text-embedding")
public class TextEmbeddingController {

    private final TextEmbeddingService textEmbeddingService;

    @PostMapping(value = "/generate/{collectionName}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String createCollection(@PathVariable String collectionName) throws ExecutionException, InterruptedException {
        return textEmbeddingService.createCollection(collectionName);
    }

    @PostMapping(value = "/retrieve/{input}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EmbeddingItem> retrieveEmbedding(@PathVariable String input) {
        return textEmbeddingService.retrieveEmbeddings(input);
    }

    @PostMapping(value = "/store-embedding", produces = MediaType.TEXT_PLAIN_VALUE)
    public String storeEmbedding(@RequestBody Map<String, Object> input) throws ExecutionException, InterruptedException {
        return textEmbeddingService.storeEmbeddingFromInput(input);
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public String search(@RequestParam String input) throws ExecutionException, InterruptedException {
        return textEmbeddingService.search(input, Optional.empty()).toString();
    }
}
