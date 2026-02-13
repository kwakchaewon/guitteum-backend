package com.guitteum.api.admin;

import com.guitteum.infra.elasticsearch.SpeechIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
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
    private final JobLauncher jobLauncher;
    private final Job embeddingJob;
    private final Job collectJob;

    @PostMapping("/index/speeches")
    public ResponseEntity<Map<String, Object>> reindexSpeeches() {
        long count = speechIndexService.indexAll();
        return ResponseEntity.ok(Map.of(
                "message", "Reindexing completed",
                "indexedCount", count
        ));
    }

    @PostMapping("/batch/collect")
    public ResponseEntity<Map<String, Object>> runCollectBatch() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(collectJob, params);

            return ResponseEntity.ok(Map.of(
                    "message", "Speech collect batch started"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Batch failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/batch/embed")
    public ResponseEntity<Map<String, Object>> runEmbeddingBatch() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(embeddingJob, params);

            return ResponseEntity.ok(Map.of(
                    "message", "Embedding batch started"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Batch failed: " + e.getMessage()
            ));
        }
    }
}
