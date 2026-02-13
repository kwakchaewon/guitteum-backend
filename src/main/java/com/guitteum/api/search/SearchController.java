package com.guitteum.api.search;

import com.guitteum.domain.speech.dto.SpeechSearchResponse;
import com.guitteum.domain.speech.service.SpeechSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/speeches")
@RequiredArgsConstructor
public class SearchController {

    private final SpeechSearchService speechSearchService;

    @GetMapping("/search")
    public ResponseEntity<Page<SpeechSearchResponse>> search(
            @RequestParam String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(
                speechSearchService.search(query, category, dateFrom, dateTo, pageable)
        );
    }
}
