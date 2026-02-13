package com.guitteum.infra.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.AnalyzeRequest;
import co.elastic.clients.elasticsearch.indices.AnalyzeResponse;
import co.elastic.clients.elasticsearch.indices.analyze.AnalyzeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoriAnalyzeService {

    private final ElasticsearchClient elasticsearchClient;

    private static final Set<String> STOPWORDS = Set.of(
            "것", "수", "등", "때", "위", "바", "중", "더", "또", "이", "그", "저",
            "년", "월", "일", "대통령", "우리", "나라", "정부", "국민", "사회", "오늘",
            "이번", "모든", "함께", "매우", "가장", "통해", "대한", "하나", "지금", "여러"
    );

    private static final int MAX_TEXT_LENGTH = 10000;

    public List<String> analyze(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<String> allTokens = new ArrayList<>();

        // 긴 텍스트는 분할하여 분석
        for (int i = 0; i < text.length(); i += MAX_TEXT_LENGTH) {
            String chunk = text.substring(i, Math.min(i + MAX_TEXT_LENGTH, text.length()));
            allTokens.addAll(analyzeChunk(chunk));
        }

        return allTokens;
    }

    private List<String> analyzeChunk(String text) {
        try {
            AnalyzeResponse response = elasticsearchClient.indices().analyze(
                    AnalyzeRequest.of(a -> a
                            .index("speeches")
                            .analyzer("korean")
                            .text(text)
                    )
            );

            return response.tokens().stream()
                    .map(AnalyzeToken::token)
                    .filter(token -> token.length() > 1)
                    .filter(token -> !STOPWORDS.contains(token))
                    .toList();
        } catch (IOException e) {
            log.error("Nori 형태소 분석 실패: {}", e.getMessage());
            return List.of();
        }
    }
}
