package com.guitteum.domain.speech.repository;

import com.guitteum.domain.speech.entity.Speech;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpeechRepository extends JpaRepository<Speech, Long> {
}
