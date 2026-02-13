-- 연설문 카테고리 컬럼 추가
ALTER TABLE speeches ADD COLUMN category VARCHAR(50) DEFAULT NULL;
ALTER TABLE speeches ADD INDEX idx_category (category);
