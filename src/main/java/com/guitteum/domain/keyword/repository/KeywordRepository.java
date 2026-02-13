package com.guitteum.domain.keyword.repository;

import com.guitteum.domain.keyword.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    @Query("SELECT k.word, SUM(k.frequency) AS total FROM Keyword k " +
            "GROUP BY k.word ORDER BY total DESC LIMIT :limit")
    List<Object[]> findTopKeywords(@Param("limit") int limit);

    @Query("SELECT k FROM Keyword k WHERE k.word = :word " +
            "AND k.speechMonth BETWEEN :from AND :to ORDER BY k.speechMonth")
    List<Keyword> findTrendByWord(@Param("word") String word,
                                  @Param("from") String from,
                                  @Param("to") String to);

    @Query("SELECT COUNT(DISTINCT k.word) FROM Keyword k")
    long countDistinctWords();

    void deleteAllInBatch();
}
