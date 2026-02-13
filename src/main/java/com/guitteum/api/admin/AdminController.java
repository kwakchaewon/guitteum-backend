package com.guitteum.api.admin;

import com.guitteum.infra.elasticsearch.SpeechIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SpeechIndexService speechIndexService;

    @PostMapping("/index/speeches")
    public ResponseEntity<Map<String, Object>> reindexSpeeches() {
        long count = speechIndexService.indexAll();
        return ResponseEntity.ok(Map.of(
                "message", "Reindexing completed",
                "indexedCount", count
        ));
    }
}
