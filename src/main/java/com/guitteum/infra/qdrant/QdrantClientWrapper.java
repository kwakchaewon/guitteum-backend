package com.guitteum.infra.qdrant;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Common;
import io.qdrant.client.grpc.Points;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static io.qdrant.client.ConditionFactory.matchKeyword;
import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;
import static io.qdrant.client.WithPayloadSelectorFactory.enable;

@Slf4j
@Component
@RequiredArgsConstructor
public class QdrantClientWrapper {

    private final QdrantClient qdrantClient;

    @Value("${qdrant.collection-name}")
    private String collectionName;

    private static final int VECTOR_SIZE = 1536;

    public void createCollectionIfNotExists() {
        try {
            boolean exists = qdrantClient.collectionExistsAsync(collectionName).get();
            if (!exists) {
                qdrantClient.createCollectionAsync(collectionName,
                        Collections.VectorParams.newBuilder()
                                .setSize(VECTOR_SIZE)
                                .setDistance(Collections.Distance.Cosine)
                                .build()
                ).get();
                log.info("Created Qdrant collection: {}", collectionName);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to create Qdrant collection", e);
        }
    }

    public String upsert(float[] vector, Long speechId, int chunkIndex, String content) {
        try {
            UUID pointId = UUID.randomUUID();

            Points.PointStruct point = Points.PointStruct.newBuilder()
                    .setId(id(pointId))
                    .setVectors(vectors(toList(vector)))
                    .putAllPayload(Map.of(
                            "speechId", value(speechId),
                            "chunkIndex", value(chunkIndex),
                            "content", value(content)
                    ))
                    .build();

            qdrantClient.upsertAsync(collectionName, List.of(point)).get();
            return pointId.toString();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to upsert vector", e);
        }
    }

    public List<Points.ScoredPoint> search(float[] queryVector, int topK) {
        try {
            return qdrantClient.searchAsync(
                    Points.SearchPoints.newBuilder()
                            .setCollectionName(collectionName)
                            .addAllVector(toList(queryVector))
                            .setLimit(topK)
                            .setWithPayload(enable(true))
                            .build()
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to search vectors", e);
        }
    }

    public void deleteByFilter(Long speechId) {
        try {
            qdrantClient.deleteAsync(collectionName,
                    Common.Filter.newBuilder()
                            .addMust(matchKeyword("speechId", String.valueOf(speechId)))
                            .build()
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete vectors", e);
        }
    }

    private List<Float> toList(float[] array) {
        List<Float> list = new java.util.ArrayList<>(array.length);
        for (float f : array) {
            list.add(f);
        }
        return list;
    }
}
