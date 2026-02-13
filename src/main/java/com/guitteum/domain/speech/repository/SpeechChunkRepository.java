package com.guitteum.domain.speech.repository;

import com.guitteum.domain.speech.entity.SpeechChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpeechChunkRepository extends JpaRepository<SpeechChunk, Long> {

    List<SpeechChunk> findBySpeechIdOrderByChunkIndex(Long speechId);
}
