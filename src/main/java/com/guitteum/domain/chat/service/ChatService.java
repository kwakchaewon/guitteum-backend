package com.guitteum.domain.chat.service;

import com.guitteum.domain.chat.dto.ChatRequest;
import com.guitteum.domain.chat.dto.ChatResponse;
import com.guitteum.domain.chat.entity.ChatMessage;
import com.guitteum.domain.chat.entity.ChatSession;
import com.guitteum.domain.chat.entity.MessageSource;
import com.guitteum.domain.chat.repository.ChatMessageRepository;
import com.guitteum.domain.chat.repository.ChatSessionRepository;
import com.guitteum.domain.speech.dto.VectorSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final RagService ragService;

    @Transactional
    public ChatResponse chat(ChatRequest request) {
        // 1. 세션 조회 또는 생성
        ChatSession session = getOrCreateSession(request.sessionId());

        // 2. 사용자 메시지 저장
        ChatMessage userMessage = ChatMessage.builder()
                .session(session)
                .role(ChatMessage.Role.USER)
                .content(request.message())
                .build();
        chatMessageRepository.save(userMessage);

        // 3. RAG 처리
        RagService.RagResult ragResult = ragService.ask(request.message());

        // 4. 어시스턴트 메시지 저장
        ChatMessage assistantMessage = ChatMessage.builder()
                .session(session)
                .role(ChatMessage.Role.ASSISTANT)
                .content(ragResult.answer())
                .build();
        chatMessageRepository.save(assistantMessage);

        // 5. 출처 저장
        for (VectorSearchResponse source : ragResult.sources()) {
            MessageSource messageSource = MessageSource.builder()
                    .message(assistantMessage)
                    .speechId(source.speechId())
                    .chunkId(source.chunkId())
                    .relevanceScore(source.score())
                    .build();
            assistantMessage.addSource(messageSource);
        }
        chatMessageRepository.save(assistantMessage);

        // 6. 응답 생성
        List<ChatResponse.SourceInfo> sourceInfos = ragResult.sources().stream()
                .map(s -> new ChatResponse.SourceInfo(s.speechId(), s.chunkIndex(), s.content(), s.score()))
                .toList();

        return new ChatResponse(session.getSessionId(), ragResult.answer(), sourceInfos);
    }

    private ChatSession getOrCreateSession(String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            return chatSessionRepository.findBySessionId(sessionId)
                    .orElseGet(() -> chatSessionRepository.save(
                            ChatSession.builder().sessionId(sessionId).build()
                    ));
        }
        return chatSessionRepository.save(ChatSession.builder().build());
    }
}
