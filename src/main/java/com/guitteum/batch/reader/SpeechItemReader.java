package com.guitteum.batch.reader;

import com.guitteum.domain.speech.entity.Speech;
import com.guitteum.domain.speech.repository.SpeechRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SpeechItemReader {

    private final SpeechRepository speechRepository;

    @Bean
    public RepositoryItemReader<Speech> speechItemReader() {
        return new RepositoryItemReaderBuilder<Speech>()
                .name("speechItemReader")
                .repository(speechRepository)
                .methodName("findAll")
                .pageSize(10)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }
}
