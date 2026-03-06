package com.guitteum.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    @Qualifier("collectJob")
    private final Job collectJob;
    @Qualifier("embeddingJob")
    private final Job embeddingJob;

    /**
     * 서버 시작 시 자동으로 수집 + 임베딩 배치를 실행한다.
     */
    @PostConstruct
    public void onStartup() {
        runCollectAndEmbed();
    }

    /**
     * 평일(월~금) 09:00, 12:00, 15:00, 18:00에 연설문 수집 + 임베딩 배치를 실행한다.
     */
    @Scheduled(cron = "0 0 9,12,15,18 * * MON-FRI")
    public void runCollectAndEmbed() {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        try {
            log.info("수집 배치 시작");
            jobLauncher.run(collectJob, params);
            log.info("수집 배치 완료");
        } catch (Exception e) {
            log.error("수집 배치 실패: {}", e.getMessage(), e);
            return;
        }

        try {
            log.info("임베딩 배치 시작");
            jobLauncher.run(embeddingJob, params);
            log.info("임베딩 배치 완료");
        } catch (Exception e) {
            log.error("임베딩 배치 실패: {}", e.getMessage(), e);
        }
    }
}
