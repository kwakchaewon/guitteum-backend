package com.guitteum.batch.job;

import com.guitteum.domain.speech.entity.Speech;
import com.guitteum.domain.speech.entity.SpeechChunk;
import com.guitteum.infra.qdrant.QdrantClientWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class EmbeddingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final QdrantClientWrapper qdrantClientWrapper;

    @Bean
    public Job embeddingJob(Step embeddingStep) {
        return new JobBuilder("embeddingJob", jobRepository)
                .listener(qdrantCollectionListener())
                .start(embeddingStep)
                .build();
    }

    @Bean
    public Step embeddingStep(
            ItemReader<Speech> speechItemReader,
            ItemProcessor<Speech, List<SpeechChunk>> chunkProcessor,
            ItemWriter<List<SpeechChunk>> embeddingWriter
    ) {
        return new StepBuilder("embeddingStep", jobRepository)
                .<Speech, List<SpeechChunk>>chunk(5, transactionManager)
                .reader(speechItemReader)
                .processor(chunkProcessor)
                .writer(embeddingWriter)
                .build();
    }

    private JobExecutionListener qdrantCollectionListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                qdrantClientWrapper.createCollectionIfNotExists();
            }
        };
    }
}
