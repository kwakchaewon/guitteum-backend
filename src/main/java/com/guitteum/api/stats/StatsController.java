package com.guitteum.api.stats;

import com.guitteum.domain.keyword.service.KeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final KeywordService keywordService;

    @GetMapping("/keywords/top")
    public ResponseEntity<List<Map<String, Object>>> topKeywords(
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(keywordService.getTopKeywords(limit));
    }

    @GetMapping("/keywords/trend")
    public ResponseEntity<List<Map<String, Object>>> keywordTrend(
            @RequestParam String keyword,
            @RequestParam String from,
            @RequestParam String to) {
        return ResponseEntity.ok(keywordService.getKeywordTrend(keyword, from, to));
    }

    @GetMapping("/speeches/monthly")
    public ResponseEntity<List<Map<String, Object>>> monthlySpeeches() {
        return ResponseEntity.ok(keywordService.getMonthlySpeeches());
    }

    @GetMapping("/speeches/category")
    public ResponseEntity<List<Map<String, Object>>> categoryDistribution() {
        return ResponseEntity.ok(keywordService.getCategoryDistribution());
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary() {
        return ResponseEntity.ok(keywordService.getSummary());
    }
}
