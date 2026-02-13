package com.guitteum.domain.chat.service;

import com.guitteum.domain.chat.dto.ChatRequest;
import com.guitteum.domain.chat.dto.ChatResponse;
import com.guitteum.domain.chat.entity.ChatMessage;
import com.guitteum.domain.chat.entity.ChatSession;
import com.guitteum.domain.chat.entity.MessageSource;
import com.guitteum.domain.chat.repository.ChatMessageRepository;
import com.guitteum.domain.chat.repository.ChatSessionRepository;
import com.guitteum.domain.speech.dto.VectorSearchResponse;
import com.guitteum.infra.openai.OpenAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int MAX_HISTORY_MESSAGES = 6; // 최근 3턴 (user+assistant 각 1)

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final RagService ragService;

    @Transactional
    public ChatResponse chat(ChatRequest request) {
        // 1. 세션 조회 또는 생성
        ChatSession session = getOrCreateSession(request.sessionId());

        // 2. 대화 이력 로드
        List<OpenAiClient.ChatMessage> history = loadHistory(session);

        // 3. 사용자 메시지 저장
        ChatMessage userMessage = ChatMessage.builder()
                .session(session)
                .role(ChatMessage.Role.USER)
                .content(request.message())
                .build();
        chatMessageRepository.save(userMessage);

        // 4. RAG 처리 (이력 포함)
        RagService.RagResult ragResult = ragService.ask(request.message(), history);

        // 5. 어시스턴트 메시지 저장
        ChatMessage assistantMessage = ChatMessage.builder()
                .session(session)
                .role(ChatMessage.Role.ASSISTANT)
                .content(ragResult.answer())
                .build();
        chatMessageRepository.save(assistantMessage);

        // 6. 출처 저장
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

        // 7. 응답 생성
        List<ChatResponse.SourceInfo> sourceInfos = ragResult.sources().stream()
                .map(s -> new ChatResponse.SourceInfo(s.speechId(), s.chunkIndex(), s.content(), s.score()))
                .toList();

        return new ChatResponse(session.getSessionId(), ragResult.answer(), sourceInfos);
    }

    public SseEmitter chatStream(ChatRequest request) {
        SseEmitter emitter = new SseEmitter(60_000L);

        new Thread(() -> {
            try {
                // 1. 세션 조회/생성 + 이력 로드 + 사용자 메시지 저장
                ChatSession session = getOrCreateSession(request.sessionId());
                List<OpenAiClient.ChatMessage> history = loadHistory(session);

                ChatMessage userMessage = ChatMessage.builder()
                        .session(session)
                        .role(ChatMessage.Role.USER)
                        .content(request.message())
                        .build();
                chatMessageRepository.save(userMessage);

                // 2. 벡터 검색 → sources 이벤트 전송
                List<VectorSearchResponse> sources = ragService.askStream(
                        request.message(),
                        history,
                        token -> {
                            try {
                                emitter.send(SseEmitter.event().name("token").data(token));
                            } catch (IOException e) {
                                log.warn("SSE token send failed", e);
                            }
                        }
                );

                // 3. sources 이벤트 전송
                List<ChatResponse.SourceInfo> sourceInfos = sources.stream()
                        .map(s -> new ChatResponse.SourceInfo(s.speechId(), s.chunkIndex(), s.content(), s.score()))
                        .toList();
                emitter.send(SseEmitter.event().name("sources").data(sourceInfos));

                // 4. done 이벤트
                emitter.send(SseEmitter.event().name("done").data(session.getSessionId()));
                emitter.complete();

            } catch (Exception e) {
                log.error("SSE streaming failed", e);
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(String sessionId) {
        ChatSession session = chatSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId));
        return chatMessageRepository.findBySessionOrderByCreatedAtAsc(session);
    }

    @Transactional
    public void deleteSession(String sessionId) {
        chatSessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            chatMessageRepository.deleteAll(chatMessageRepository.findBySessionOrderByCreatedAtAsc(session));
            chatSessionRepository.delete(session);
        });
    }

    private List<OpenAiClient.ChatMessage> loadHistory(ChatSession session) {
        List<ChatMessage> recent = chatMessageRepository.findTop6BySessionOrderByCreatedAtDesc(session);
        if (recent.isEmpty()) {
            return List.of();
        }
        List<ChatMessage> ordered = new ArrayList<>(recent);
        Collections.reverse(ordered);
        return ordered.stream()
                .map(m -> new OpenAiClient.ChatMessage(m.getRole().name().toLowerCase(), m.getContent()))
                .toList();
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
