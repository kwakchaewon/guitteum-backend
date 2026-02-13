package com.guitteum.batch.job;

import com.guitteum.domain.speech.entity.Speech;
import com.guitteum.domain.speech.repository.SpeechRepository;
import com.guitteum.global.common.Category;
import com.guitteum.infra.elasticsearch.SpeechIndexService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CategoryClassifyJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SpeechRepository speechRepository;
    private final SpeechIndexService speechIndexService;

    @Bean
    public Job classifyJob(Step classifyStep) {
        return new JobBuilder("classifyJob", jobRepository)
                .start(classifyStep)
                .build();
    }

    @Bean
    public Step classifyStep() {
        return new StepBuilder("classifyStep", jobRepository)
                .tasklet(classifyTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet classifyTasklet() {
        return (contribution, chunkContext) -> {
            List<Speech> speeches = speechRepository.findAll();
            log.info("카테고리 분류 시작: {}건", speeches.size());

            Map<String, Integer> stats = new HashMap<>();
            for (Speech speech : speeches) {
                Category category = Category.classify(speech.getContent());
                speech.updateCategory(category.name());
                stats.merge(category.name(), 1, Integer::sum);
            }

            speechRepository.saveAll(speeches);
            log.info("카테고리 분류 완료: {}", stats);

            // ES 재인덱싱
            long indexed = speechIndexService.indexAll();
            log.info("ES 재인덱싱 완료: {}건", indexed);

            return RepeatStatus.FINISHED;
        };
    }
}
