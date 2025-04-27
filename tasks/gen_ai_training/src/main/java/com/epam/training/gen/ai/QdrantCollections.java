package com.epam.training.gen.ai;

import com.google.common.util.concurrent.ListenableFuture;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class QdrantCollections implements CommandLineRunner {

    private final QdrantClient qdrantClient;

    @Value("${qdrant-collection-name}")
    private String collectionName;

    public QdrantCollections(QdrantClient qdrantClient) {
        this.qdrantClient = qdrantClient;
    }

    @Override
    public void run(String... args) throws Exception {
        Collections.VectorParams vectorParams = Collections.VectorParams.newBuilder()
                .setSize(1536)
                .setDistance(Collections.Distance.Cosine)
                .build();

        Collections.CreateCollection createRequest = Collections
                .CreateCollection.newBuilder()
                .setCollectionName(collectionName)
                .setVectorsConfig(
                        Collections.VectorsConfig.newBuilder()
                                .setParams(vectorParams).build())
                .build();

        ListenableFuture<Collections.CollectionOperationResponse> future = qdrantClient.createCollectionAsync(createRequest);
        Collections.CollectionOperationResponse response = future.get();
        if (response.getResult()) {
            log.info("Collection {} created!!", collectionName);
        } else {
            log.error("Create collection: {} failed", collectionName);
        }
    }
}
