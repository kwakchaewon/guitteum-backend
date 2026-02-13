package com.guitteum.infra.elasticsearch;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.guitteum.infra.elasticsearch")
public class ElasticsearchConfig {
}
