package com.guitteum.batch.job;

import com.guitteum.domain.speech.entity.Speech;
import com.guitteum.domain.speech.repository.SpeechRepository;
import com.guitteum.infra.mcp.SpeechData;
import com.guitteum.infra.mcp.SpeechMcpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SpeechCollectJobConfig {

    private static final int PER_PAGE = 100;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SpeechMcpClient speechMcpClient;
    private final SpeechRepository speechRepository;

    @Bean
    public Job collectJob(Step collectStep) {
        return new JobBuilder("collectJob", jobRepository)
                .start(collectStep)
                .build();
    }

    @Bean
    public Step collectStep() {
        return new StepBuilder("collectStep", jobRepository)
                .tasklet(collectTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet collectTasklet() {
        return (contribution, chunkContext) -> {
            AtomicInteger saved = new AtomicInteger(0);
            AtomicInteger skipped = new AtomicInteger(0);

            int total = speechMcpClient.collectAll(PER_PAGE, speeches -> {
                for (SpeechData data : speeches) {
                    try {
                        Speech speech = toSpeech(data);
                        if (speech == null) {
                            skipped.incrementAndGet();
                            continue;
                        }
                        if (speechRepository.existsByTitleAndSpeechDate(speech.getTitle(), speech.getSpeechDate())) {
                            skipped.incrementAndGet();
                            continue;
                        }
                        speechRepository.save(speech);
                        saved.incrementAndGet();
                    } catch (Exception e) {
                        log.warn("연설문 저장 실패: title={}, error={}", data.title(), e.getMessage());
                        skipped.incrementAndGet();
                    }
                }
            });

            log.info("수집 완료 — 총 {}건 중 저장 {}건, 건너뜀 {}건", total, saved.get(), skipped.get());
            return RepeatStatus.FINISHED;
        };
    }

    private Speech toSpeech(SpeechData data) {
        if (data.title() == null || data.title().isBlank()) {
            return null;
        }

        String content = data.content();
        if (content == null || content.isBlank()) {
            content = "(본문 없음)";
        }

        LocalDateTime speechDate = parseSpeechDate(data);
        String eventName = buildEventName(data);

        return Speech.builder()
                .title(data.title().length() > 500 ? data.title().substring(0, 500) : data.title())
                .content(content)
                .speechDate(speechDate)
                .eventName(eventName)
                .build();
    }

    private LocalDateTime parseSpeechDate(SpeechData data) {
        // speechDate 필드 (yyyy-MM-dd 또는 yyyy.MM.dd 등)
        String dateStr = data.speechDate() != null ? data.speechDate() : data.date();
        if (dateStr != null && !dateStr.isBlank()) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
            } catch (DateTimeParseException e1) {
                try {
                    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy.MM.dd")).atStartOfDay();
                } catch (DateTimeParseException e2) {
                    // fall through
                }
            }
        }

        // speechYear 필드
        if (data.speechYear() != null) {
            return LocalDateTime.of(data.speechYear(), 1, 1, 0, 0);
        }

        return LocalDateTime.of(2000, 1, 1, 0, 0);
    }

    private String buildEventName(SpeechData data) {
        StringBuilder sb = new StringBuilder();
        if (data.president() != null && !data.president().isBlank()) {
            sb.append(data.president());
        }
        if (data.location() != null && !data.location().isBlank()) {
            if (!sb.isEmpty()) sb.append(" - ");
            sb.append(data.location());
        }
        String result = sb.toString();
        return result.length() > 200 ? result.substring(0, 200) : result;
    }
}
