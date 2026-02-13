package com.guitteum.domain.speech.service;

import com.guitteum.domain.speech.dto.SpeechDetailResponse;
import com.guitteum.domain.speech.dto.SpeechResponse;
import com.guitteum.domain.speech.repository.SpeechRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpeechService {

    private final SpeechRepository speechRepository;

    public Page<SpeechResponse> getSpeeches(Pageable pageable) {
        return speechRepository.findAll(pageable)
                .map(SpeechResponse::from);
    }

    public SpeechDetailResponse getSpeech(Long id) {
        return speechRepository.findById(id)
                .map(SpeechDetailResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Speech not found: " + id));
    }
}
