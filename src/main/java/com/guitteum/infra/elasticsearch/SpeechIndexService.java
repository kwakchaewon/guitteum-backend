package com.guitteum.infra.elasticsearch;

import com.guitteum.domain.speech.entity.Speech;
import com.guitteum.domain.speech.repository.SpeechRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpeechIndexService {

    private final SpeechRepository speechRepository;
    private final SpeechSearchRepository speechSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public long indexAll() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(SpeechDocument.class);
        if (indexOps.exists()) {
            indexOps.delete();
        }
        indexOps.createWithMapping();

        List<Speech> speeches = speechRepository.findAll();
        List<SpeechDocument> documents = speeches.stream()
                .map(this::toDocument)
                .toList();

        speechSearchRepository.saveAll(documents);
        log.info("Indexed {} speeches to Elasticsearch", documents.size());
        return documents.size();
    }

    public void indexOne(Speech speech) {
        speechSearchRepository.save(toDocument(speech));
    }

    public void deleteOne(Long id) {
        speechSearchRepository.deleteById(id);
    }

    private SpeechDocument toDocument(Speech speech) {
        return SpeechDocument.builder()
                .id(speech.getId())
                .title(speech.getTitle())
                .content(speech.getContent())
                .speechDate(speech.getSpeechDate())
                .eventName(speech.getEventName())
                .category(speech.getCategory())
                .createdAt(speech.getCreatedAt())
                .build();
    }
}
