package com.guitteum.domain.speech.service;

import com.guitteum.domain.speech.dto.SpeechSearchResponse;
import com.guitteum.infra.elasticsearch.SpeechDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpeechSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public Page<SpeechSearchResponse> search(
            String query,
            String category,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Pageable pageable
    ) {
        NativeQuery searchQuery = buildSearchQuery(query, category, dateFrom, dateTo, pageable);

        SearchHits<SpeechDocument> searchHits =
                elasticsearchOperations.search(searchQuery, SpeechDocument.class);

        List<SpeechSearchResponse> results = searchHits.getSearchHits().stream()
                .map(this::toSearchResponse)
                .toList();

        return new PageImpl<>(results, pageable, searchHits.getTotalHits());
    }

    private NativeQuery buildSearchQuery(
            String query,
            String category,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Pageable pageable
    ) {
        return NativeQuery.builder()
                .withQuery(q -> q.bool(b -> {
                    b.must(m -> m.multiMatch(mm -> mm
                            .query(query)
                            .fields("title^3", "content")
                            .fuzziness("AUTO")
                    ));

                    if (category != null && !category.isBlank()) {
                        b.filter(f -> f.term(t -> t
                                .field("category")
                                .value(category)
                        ));
                    }
                    if (dateFrom != null) {
                        b.filter(f -> f.range(r -> r
                                .field("speechDate")
                                .gte(co.elastic.clients.json.JsonData.of(dateFrom.toString()))
                        ));
                    }
                    if (dateTo != null) {
                        b.filter(f -> f.range(r -> r
                                .field("speechDate")
                                .lte(co.elastic.clients.json.JsonData.of(dateTo.toString()))
                        ));
                    }

                    return b;
                }))
                .withPageable(pageable)
                .withHighlightQuery(buildHighlightQuery())
                .withSort(s -> s.score(sc -> sc.order(
                        co.elastic.clients.elasticsearch._types.SortOrder.Desc)))
                .build();
    }

    private HighlightQuery buildHighlightQuery() {
        HighlightParameters params = HighlightParameters.builder()
                .withPreTags("<em>")
                .withPostTags("</em>")
                .withFragmentSize(150)
                .withNumberOfFragments(3)
                .build();

        List<HighlightField> fields = List.of(
                new HighlightField("title"),
                new HighlightField("content")
        );

        return new HighlightQuery(new Highlight(params, fields), SpeechDocument.class);
    }

    private SpeechSearchResponse toSearchResponse(SearchHit<SpeechDocument> hit) {
        SpeechDocument doc = hit.getContent();

        List<String> titleHighlights = hit.getHighlightField("title");
        List<String> contentHighlights = hit.getHighlightField("content");

        return new SpeechSearchResponse(
                doc.getId(),
                doc.getTitle(),
                doc.getSpeechDate(),
                doc.getEventName(),
                doc.getCategory(),
                titleHighlights.isEmpty() ? Collections.emptyList() : titleHighlights,
                contentHighlights.isEmpty() ? Collections.emptyList() : contentHighlights
        );
    }
}
