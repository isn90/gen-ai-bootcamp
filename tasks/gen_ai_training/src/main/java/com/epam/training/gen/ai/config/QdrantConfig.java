package com.epam.training.gen.ai.config;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutionException;

@Slf4j
@Configuration
public class QdrantConfig {

    @Value("${qdrant-host}")
    private String host;
    @Value("${qdrant-port}")
    private int port;
    /**
     * Creates a {@link QdrantClient} bean for interacting with the Qdrant service.
     *
     * @return an instance of {@link QdrantClient}
     */
    @Bean
    public QdrantClient qdrantClient() throws ExecutionException, InterruptedException {
        return new QdrantClient(QdrantGrpcClient
                .newBuilder(host, port, false).build());
    }

}
