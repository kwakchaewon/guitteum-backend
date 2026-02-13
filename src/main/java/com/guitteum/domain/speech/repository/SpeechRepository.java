package com.guitteum.domain.speech.repository;

import com.guitteum.domain.speech.entity.Speech;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface SpeechRepository extends JpaRepository<Speech, Long> {

    boolean existsByTitleAndSpeechDate(String title, LocalDateTime speechDate);

    @Query("SELECT FUNCTION('DATE_FORMAT', s.speechDate, '%Y-%m') AS month, COUNT(s) " +
            "FROM Speech s GROUP BY FUNCTION('DATE_FORMAT', s.speechDate, '%Y-%m') ORDER BY month")
    List<Object[]> countByMonth();

    @Query("SELECT s.category, COUNT(s) FROM Speech s WHERE s.category IS NOT NULL " +
            "GROUP BY s.category ORDER BY COUNT(s) DESC")
    List<Object[]> countByCategory();
}
