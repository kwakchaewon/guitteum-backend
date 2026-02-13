package com.guitteum.domain.chat.repository;

import com.guitteum.domain.chat.entity.ChatMessage;
import com.guitteum.domain.chat.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionOrderByCreatedAtAsc(ChatSession session);
}
