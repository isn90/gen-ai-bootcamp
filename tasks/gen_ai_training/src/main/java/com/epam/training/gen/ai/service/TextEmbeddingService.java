package com.epam.training.gen.ai.service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.EmbeddingItem;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.EmbeddingsOptions;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.common.util.concurrent.ListenableFuture;
import reactor.core.publisher.Mono;
import static io.qdrant.client.ValueFactory.value;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.VectorsFactory.vectors;
import static io.qdrant.client.WithPayloadSelectorFactory.enable;

@Slf4j
@Service
public class TextEmbeddingService {

    private final QdrantClient qdrantClient;
    private final OpenAIAsyncClient openAIAsyncClient;

    @Value("${client-openai-deployment-name}")
    private String deploymentName;

    @Value("${qdrant-collection-name}")
    private String collectionName;


    public TextEmbeddingService(QdrantClient qdrantClient, @Qualifier("genericOpenAiAsyncClient") OpenAIAsyncClient openAIAsyncClient) {
        this.qdrantClient = qdrantClient;
        this.openAIAsyncClient = openAIAsyncClient;
    }

    public String createCollection(String collectionName) throws ExecutionException, InterruptedException {
        // Qdrant Documentation : Setting vector config
        Collections.VectorParams vectorParams = Collections.VectorParams.newBuilder()
                .setSize(1536)
                .setDistance(Collections.Distance.Cosine)
                .build();

        // Create the collection request
        Collections.CreateCollection createRequest = Collections
                .CreateCollection.newBuilder()
                .setCollectionName(collectionName)
                .setVectorsConfig(
                        Collections.VectorsConfig.newBuilder()
                                .setParams(vectorParams).build())
                .build();

        ListenableFuture<Collections.CollectionOperationResponse> future = qdrantClient.createCollectionAsync(createRequest);
        // Handle the response
        Collections.CollectionOperationResponse response = future.get(); // blocks until result

        if (response.getResult()) {
            log.info("Collection {} created successfully!", collectionName);
            return "Collections created successfully " + collectionName;
        } else {
            log.error("Failed to create collection: {}", collectionName);
            return "Error creating collection " + collectionName;
        }
    }

    public String storeEmbeddingFromInput(Map<String, Object> input) throws ExecutionException, InterruptedException {
        String genericSummarizedString = getInputString(input);
        List<EmbeddingItem> embeddingItemsList = retrieveEmbeddings(genericSummarizedString);
        var points = new ArrayList<List<Float>>();
        embeddingItemsList.forEach(
                embeddingItem ->
                        points.add(new ArrayList<>(embeddingItem.getEmbedding())));
        var pointStructs = new ArrayList<Points.PointStruct>();
        points.forEach(point ->
                pointStructs.add(getPointStruct(point, input)));
        return saveVector(pointStructs);
    }

    public List<EmbeddingItem> retrieveEmbeddings(String text) {
        var embeddingsOptions = new EmbeddingsOptions(List.of(text));
        var embeddings = openAIAsyncClient.getEmbeddings("text-embedding-ada-002", embeddingsOptions);

        return Objects.requireNonNull(embeddings.block()).getData();
    }

    public List<Points.ScoredPoint> search(String input, Optional<String> collectionsName) throws ExecutionException, InterruptedException {
        var inputEmbeddings = new ArrayList<Float>();
        log.info("Start of search");
        retrieveEmbeddings(input).forEach(embeddingItem ->
                inputEmbeddings.addAll(embeddingItem.getEmbedding())
        );
        List<Points.ScoredPoint> result = qdrantClient.searchAsync(Points.SearchPoints.newBuilder()
                .setCollectionName(collectionsName.orElseGet(() -> collectionName))
                .addAllVector(inputEmbeddings)
                .setLimit(10)
                .setWithPayload(enable(true))
                .build()).get();
        log.info("Search completed!! result={}", result);
        return result;
    }

    public String saveVector(ArrayList<Points.PointStruct> pointStructs) throws InterruptedException, ExecutionException {
        Points.UpdateResult response = qdrantClient.upsertAsync(collectionName, pointStructs).get();
        if (response!=null && response.getStatus() == Points.UpdateStatus.Completed) {
            log.info("Upsert successful! Operation ID: {}  ", response.getOperationId());
            return "Vector successfully stored successfully with Point id : " + pointStructs;
        } else {
            log.error("Upsert failed with status:  {} ", (response != null ? response.getStatus() : "null"));
            return "Failed to store vector";
        }
    }

    private String getInputString(Map<String, Object> input) {
        StringBuilder inputStr = new StringBuilder();
        input.keySet().forEach(key -> {
            inputStr.append(key).append("=").append(input.get(key)).append(";");
        });
        String result = inputStr.toString();
        log.info("result={}", result);
        return result;
    }

    private Points.PointStruct getPointStruct(List<Float> point, Map<String, Object> input) {
        Map<String, JsonWithInt.Value> payloadMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            payloadMap.put(key, value((String) value));
            System.out.println(key + ": " + value);
        }

        Points.PointStruct pointStruct = Points.PointStruct.newBuilder()
                .setId(id(UUID.randomUUID()))
                .setVectors(vectors(point))
                .putAllPayload(payloadMap)
                .build();
        log.info("Point struct: {}", pointStruct.getPayloadMap());
        return pointStruct;
    }
}
