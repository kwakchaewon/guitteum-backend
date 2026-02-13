package com.guitteum.domain.speech.service;

import com.guitteum.domain.speech.dto.VectorSearchResponse;
import com.guitteum.infra.openai.OpenAiClient;
import com.guitteum.infra.qdrant.QdrantClientWrapper;
import io.qdrant.client.grpc.Points;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorSearchService {

    private static final Duration EMBEDDING_CACHE_TTL = Duration.ofHours(24);
    private static final String CACHE_KEY_PREFIX = "embed:";

    private final OpenAiClient openAiClient;
    private final QdrantClientWrapper qdrantClientWrapper;
    private final RedisTemplate<String, byte[]> redisTemplate;

    public List<VectorSearchResponse> search(String query, int topK) {
        float[] queryVector = getEmbeddingWithCache(query);

        List<Points.ScoredPoint> results = qdrantClientWrapper.search(queryVector, topK);

        return results.stream()
                .map(this::toResponse)
                .toList();
    }

    private float[] getEmbeddingWithCache(String query) {
        String cacheKey = CACHE_KEY_PREFIX + hash(query);

        byte[] cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("Embedding cache hit: key={}", cacheKey);
            return bytesToFloats(cached);
        }

        float[] embedding = openAiClient.embed(query);
        redisTemplate.opsForValue().set(cacheKey, floatsToBytes(embedding), EMBEDDING_CACHE_TTL);
        log.debug("Embedding cache miss, stored: key={}", cacheKey);
        return embedding;
    }

    private String hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 16);
        } catch (Exception e) {
            throw new RuntimeException("Hash failed", e);
        }
    }

    private byte[] floatsToBytes(float[] floats) {
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4);
        for (float f : floats) {
            buffer.putFloat(f);
        }
        return buffer.array();
    }

    private float[] bytesToFloats(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        float[] floats = new float[bytes.length / 4];
        for (int i = 0; i < floats.length; i++) {
            floats[i] = buffer.getFloat();
        }
        return floats;
    }

    private VectorSearchResponse toResponse(Points.ScoredPoint point) {
        var payload = point.getPayloadMap();

        long speechId = payload.containsKey("speechId")
                ? payload.get("speechId").getIntegerValue() : 0L;
        int chunkIndex = payload.containsKey("chunkIndex")
                ? (int) payload.get("chunkIndex").getIntegerValue() : 0;
        String content = payload.containsKey("content")
                ? payload.get("content").getStringValue() : "";

        return new VectorSearchResponse(
                null,
                speechId,
                chunkIndex,
                content,
                point.getScore()
        );
    }
}
