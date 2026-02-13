package com.guitteum.api.speech;

import com.guitteum.domain.speech.dto.SpeechDetailResponse;
import com.guitteum.domain.speech.dto.SpeechResponse;
import com.guitteum.domain.speech.service.SpeechService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/speeches")
@RequiredArgsConstructor
public class SpeechController {

    private final SpeechService speechService;

    @GetMapping
    public ResponseEntity<Page<SpeechResponse>> getSpeeches(
            @PageableDefault(size = 20, sort = "speechDate") Pageable pageable) {
        return ResponseEntity.ok(speechService.getSpeeches(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpeechDetailResponse> getSpeech(@PathVariable Long id) {
        return ResponseEntity.ok(speechService.getSpeech(id));
    }
}
