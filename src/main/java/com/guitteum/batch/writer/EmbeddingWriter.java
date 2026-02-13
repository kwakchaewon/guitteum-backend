package com.guitteum.batch.writer;

import com.guitteum.domain.speech.entity.SpeechChunk;
import com.guitteum.domain.speech.repository.SpeechChunkRepository;
import com.guitteum.infra.openai.OpenAiClient;
import com.guitteum.infra.qdrant.QdrantClientWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddingWriter implements ItemWriter<List<SpeechChunk>> {

    private final SpeechChunkRepository speechChunkRepository;
    private final OpenAiClient openAiClient;
    private final QdrantClientWrapper qdrantClientWrapper;

    @Override
    public void write(Chunk<? extends List<SpeechChunk>> items) {
        qdrantClientWrapper.createCollectionIfNotExists();

        for (List<SpeechChunk> chunks : items) {
            // 1. DB에 청크 저장
            List<SpeechChunk> savedChunks = speechChunkRepository.saveAll(chunks);

            // 2. 임베딩 생성 (배치)
            List<String> texts = savedChunks.stream()
                    .map(SpeechChunk::getContent)
                    .toList();
            List<float[]> embeddings = openAiClient.embedBatch(texts);

            // 3. Qdrant에 벡터 저장
            for (int i = 0; i < savedChunks.size(); i++) {
                SpeechChunk chunk = savedChunks.get(i);
                float[] vector = embeddings.get(i);

                String vectorId = qdrantClientWrapper.upsert(
                        vector,
                        chunk.getSpeechId(),
                        chunk.getChunkIndex(),
                        chunk.getContent()
                );

                log.debug("Embedded chunk [{}/{}] speechId={}, vectorId={}",
                        chunk.getChunkIndex(), savedChunks.size(),
                        chunk.getSpeechId(), vectorId);
            }

            log.info("Processed {} chunks for speechId={}", savedChunks.size(),
                    savedChunks.isEmpty() ? "?" : savedChunks.get(0).getSpeechId());
        }
    }
}
