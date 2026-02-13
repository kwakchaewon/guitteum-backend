package com.guitteum.domain.speech.service;

import com.guitteum.domain.speech.entity.Speech;
import com.guitteum.domain.speech.entity.SpeechChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ChunkingService {

    private static final int CHUNK_SIZE = 500;
    private static final int OVERLAP_SIZE = 100;
    private static final Pattern SENTENCE_BOUNDARY = Pattern.compile("(?<=[.!?。]\\s)");

    public List<SpeechChunk> chunkSpeech(Speech speech) {
        String content = speech.getContent();
        if (content == null || content.isBlank()) {
            return List.of();
        }

        List<String> chunks = splitIntoChunks(content);
        List<SpeechChunk> speechChunks = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            speechChunks.add(SpeechChunk.builder()
                    .speechId(speech.getId())
                    .chunkIndex(i)
                    .content(chunks.get(i))
                    .build());
        }

        return speechChunks;
    }

    private List<String> splitIntoChunks(String text) {
        String[] sentences = SENTENCE_BOUNDARY.split(text);
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String sentence : sentences) {
            if (current.length() + sentence.length() > CHUNK_SIZE && !current.isEmpty()) {
                chunks.add(current.toString().trim());

                // 오버랩: 마지막 부분을 다음 청크에 포함
                String overlap = getOverlap(current.toString());
                current = new StringBuilder(overlap);
            }
            current.append(sentence);
        }

        if (!current.isEmpty()) {
            String lastChunk = current.toString().trim();
            // 마지막 청크가 너무 짧으면 이전 청크에 병합
            if (lastChunk.length() < OVERLAP_SIZE && !chunks.isEmpty()) {
                String prev = chunks.remove(chunks.size() - 1);
                chunks.add((prev + " " + lastChunk).trim());
            } else {
                chunks.add(lastChunk);
            }
        }

        return chunks;
    }

    private String getOverlap(String text) {
        if (text.length() <= OVERLAP_SIZE) {
            return text;
        }
        return text.substring(text.length() - OVERLAP_SIZE);
    }
}
