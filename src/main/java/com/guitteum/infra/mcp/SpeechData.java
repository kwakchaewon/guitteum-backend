package com.guitteum.infra.mcp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpeechData(
        String id,
        String president,
        String title,
        String content,
        String date,
        @JsonProperty("speech_date") String speechDate,
        @JsonProperty("speech_year") Integer speechYear,
        String location,
        @JsonProperty("source_url") String sourceUrl
) {}
