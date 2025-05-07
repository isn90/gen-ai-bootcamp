package com.epam.training.gen.ai.service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.EmbeddingItem;
import com.epam.training.gen.ai.model.RequestPayload;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.VectorsFactory.vectors;

@Slf4j
@Service
public class RAGService {

    private static final String COLLECTION_NAME = "general-rag-collection";

    private final QdrantClient qdrantClient;
    private final OpenAIAsyncClient openAIAsyncClient;
    private final TextEmbeddingService embeddingService;
    private final PromptService promptService;

    @Value("classpath:/pdf/AWS-Certified-Solutions-Architect-Professional_Exam-Guide.pdf")
    private Resource resource;

    @Value("${client-openai-deployment-name}")
    private String deploymentName;

    @Value("${qdrant-collection-name}")
    private String collectionName;

    public RAGService(QdrantClient qdrantClient, @Qualifier("genericOpenAiAsyncClient") OpenAIAsyncClient openAIAsyncClient, TextEmbeddingService embeddingService, PromptService promptService) {
        this.qdrantClient = qdrantClient;
        this.openAIAsyncClient = openAIAsyncClient;
        this.embeddingService = embeddingService;
        this.promptService = promptService;
    }

    @SneakyThrows
    public String uploadAndStoreKnowledgeFromFile() {
        Tika tika = new Tika();
        String content = tika.parseToString(resource.getInputStream());
        // Normalize line breaks
        String cleanedText = content.replaceAll("\\r\\n", "\n").replaceAll("\\\\n", "\n");
        List<EmbeddingItem> embeddingItemsList = embeddingService.retrieveEmbeddings(cleanedText);

        var points = new ArrayList<List<Float>>();
        embeddingItemsList.forEach(
                embeddingItem ->
                        points.add(new ArrayList<>(embeddingItem.getEmbedding())));
        var pointStructs = new ArrayList<Points.PointStruct>();
        points.forEach(point ->
                pointStructs.add(getPointStruct(point, content)));
        try {
            embeddingService.saveVector(pointStructs);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return "Knowledge stored in Qdrant successfully!!!";
    }

    public String getResponseFromRAG(RequestPayload request) throws ExecutionException, InterruptedException {
        List<Points.ScoredPoint> response = embeddingService.search(request.getUserPrompt(), Optional.of(COLLECTION_NAME));
        String promptWithRag = "Use the following context(from vector Collection): " +
                "\n\n" +
                response.getFirst().getPayloadMap().get("Data") +
                "\n" +
                " to get the answer for the question: " +
                request.getUserPrompt() +
                "\n\n";
        RequestPayload requestPayload = new RequestPayload();
        requestPayload.setUserPrompt(promptWithRag);
        requestPayload.setTemperature(request.getTemperature());
        requestPayload.setMaxTokens(request.getMaxTokens());
        return promptService.getPromptResponse(requestPayload);
    }

    private Points.PointStruct getPointStruct(List<Float> point, String input) {
        Map<String, JsonWithInt.Value> payloadMap = new HashMap<>();
        payloadMap.put("context", JsonWithInt.Value.newBuilder().setStringValue(Objects.requireNonNull(resource.getFilename())).build());
        payloadMap.put("Data", JsonWithInt.Value.newBuilder().setStringValue(input).build());
        Points.PointStruct pointStruct = Points.PointStruct.newBuilder()
                .setId(id(UUID.randomUUID()))
                .setVectors(vectors(point))
                .putAllPayload(payloadMap)
                .build();
        log.info("Point struct: {}", pointStruct.getPayloadMap());
        return pointStruct;
    }

    private List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            start = end - overlap;
            if (start < 0) start = 0;
        }
        return chunks;
    }
}
