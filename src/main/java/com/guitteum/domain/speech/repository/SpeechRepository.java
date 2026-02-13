package com.guitteum.domain.speech.repository;

import com.guitteum.domain.speech.entity.Speech;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface SpeechRepository extends JpaRepository<Speech, Long> {

    boolean existsByTitleAndSpeechDate(String title, LocalDateTime speechDate);
}
