package com.guitteum.batch.processor;

import com.guitteum.domain.speech.entity.Speech;
import com.guitteum.domain.speech.entity.SpeechChunk;
import com.guitteum.domain.speech.service.ChunkingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChunkProcessor implements ItemProcessor<Speech, List<SpeechChunk>> {

    private final ChunkingService chunkingService;

    @Override
    public List<SpeechChunk> process(Speech speech) {
        List<SpeechChunk> chunks = chunkingService.chunkSpeech(speech);
        log.debug("Speech [{}] '{}' → {} chunks", speech.getId(), speech.getTitle(), chunks.size());
        return chunks.isEmpty() ? null : chunks;
    }
}
