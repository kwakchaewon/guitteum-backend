-- 키워드 월별 집계 테이블
CREATE TABLE keywords (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(100) NOT NULL,
    frequency INT NOT NULL DEFAULT 0,
    speech_month VARCHAR(7) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_word_month (word, speech_month),
    INDEX idx_word (word),
    INDEX idx_speech_month (speech_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
