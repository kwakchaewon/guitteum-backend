package com.guitteum.infra.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "speeches")
@Setting(settingPath = "elasticsearch/speech-settings.json")
@Mapping(mappingPath = "elasticsearch/speech-mappings.json")
public class SpeechDocument {

    @Id
    private Long id;

    private String title;

    private String content;

    private LocalDateTime speechDate;

    private String eventName;

    @Field(type = FieldType.Keyword)
    private String category;

    private LocalDateTime createdAt;
}
