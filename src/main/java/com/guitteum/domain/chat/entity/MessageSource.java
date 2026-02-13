package com.guitteum.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "message_sources")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class MessageSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private ChatMessage message;

    @Column(name = "speech_id", nullable = false)
    private Long speechId;

    @Column(name = "chunk_id")
    private Long chunkId;

    @Column(name = "relevance_score", nullable = false)
    private Float relevanceScore;
}
