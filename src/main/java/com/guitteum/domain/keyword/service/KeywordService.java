package com.guitteum.domain.keyword.service;

import com.guitteum.domain.keyword.entity.Keyword;
import com.guitteum.domain.keyword.repository.KeywordRepository;
import com.guitteum.domain.speech.repository.SpeechRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final SpeechRepository speechRepository;

    public List<Map<String, Object>> getTopKeywords(int limit) {
        return keywordRepository.findTopKeywords(limit).stream()
                .map(row -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("word", row[0]);
                    map.put("frequency", ((Number) row[1]).longValue());
                    return map;
                })
                .toList();
    }

    public List<Map<String, Object>> getKeywordTrend(String keyword, String from, String to) {
        return keywordRepository.findTrendByWord(keyword, from, to).stream()
                .map(k -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("month", k.getSpeechMonth());
                    map.put("frequency", k.getFrequency());
                    return map;
                })
                .toList();
    }

    public List<Map<String, Object>> getMonthlySpeeches() {
        return speechRepository.countByMonth().stream()
                .map(row -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("month", row[0]);
                    map.put("count", ((Number) row[1]).longValue());
                    return map;
                })
                .toList();
    }

    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalSpeeches", speechRepository.count());
        summary.put("totalKeywords", keywordRepository.countDistinctWords());
        return summary;
    }
}
