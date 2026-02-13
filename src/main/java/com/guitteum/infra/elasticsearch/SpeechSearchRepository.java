package com.guitteum.infra.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SpeechSearchRepository extends ElasticsearchRepository<SpeechDocument, Long> {
}
