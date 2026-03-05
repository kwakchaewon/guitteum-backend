ALTER TABLE speeches ADD COLUMN speaker VARCHAR(100) DEFAULT NULL;
ALTER TABLE speeches ADD INDEX idx_speaker (speaker);
