package com.guitteum.batch.job;

import com.guitteum.domain.keyword.entity.Keyword;
import com.guitteum.domain.keyword.repository.KeywordRepository;
import com.guitteum.domain.speech.entity.Speech;
import com.guitteum.domain.speech.repository.SpeechRepository;
import com.guitteum.infra.elasticsearch.NoriAnalyzeService;
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

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KeywordExtractJobConfig {

    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SpeechRepository speechRepository;
    private final KeywordRepository keywordRepository;
    private final NoriAnalyzeService noriAnalyzeService;

    @Bean
    public Job keywordExtractJob(Step keywordExtractStep) {
        return new JobBuilder("keywordExtractJob", jobRepository)
                .start(keywordExtractStep)
                .build();
    }

    @Bean
    public Step keywordExtractStep() {
        return new StepBuilder("keywordExtractStep", jobRepository)
                .tasklet(keywordExtractTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet keywordExtractTasklet() {
        return (contribution, chunkContext) -> {
            // 기존 데이터 전체 삭제 (재집계)
            keywordRepository.deleteAllInBatch();
            log.info("기존 키워드 데이터 삭제 완료");

            List<Speech> speeches = speechRepository.findAll();
            log.info("분석 대상 연설문: {}건", speeches.size());

            // (word, month) → frequency 집계
            Map<String, Integer> aggregation = new HashMap<>();

            for (Speech speech : speeches) {
                String month = speech.getSpeechDate().format(MONTH_FORMAT);
                List<String> tokens = noriAnalyzeService.analyze(speech.getContent());

                for (String token : tokens) {
                    String key = token + "|" + month;
                    aggregation.merge(key, 1, Integer::sum);
                }
            }

            // bulk insert
            List<Keyword> keywords = aggregation.entrySet().stream()
                    .map(entry -> {
                        String[] parts = entry.getKey().split("\\|");
                        return Keyword.builder()
                                .word(parts[0])
                                .speechMonth(parts[1])
                                .frequency(entry.getValue())
                                .build();
                    })
                    .toList();

            keywordRepository.saveAll(keywords);
            log.info("키워드 추출 완료 — 총 {}개 (word, month) 조합 저장", keywords.size());

            return RepeatStatus.FINISHED;
        };
    }
}
