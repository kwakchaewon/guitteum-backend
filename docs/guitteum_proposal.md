# 귀띔 최종 기획안 v2.0

## 📌 프로젝트 개요

### 프로젝트명
**귀띔** - AI 기반 대통령 연설문 분석 플랫폼

### 핵심 가치 제안
"대통령의 정책이 궁금하신가요? AI에게 물어보세요."
- 복잡한 연설문을 읽지 않아도 핵심 정책을 즉시 파악
- 신뢰할 수 있는 출처(실제 연설문) 기반 답변
- 시간대별 정책 변화 추적

### 타겟 사용자
- 정책 연구자, 기자, 학생
- 투자자 (정책 방향성 파악)
- 일반 시민 (정부 정책 이해)

### 개발 기간
**8주 (2개월)**

### 개발자
3년차 Java 백엔드 개발자 포트폴리오용

---

## 🎯 핵심 기능 (우선순위)

### 1. RAG 기반 정책 챗봇 ⭐⭐⭐ (메인 기능)

**사용자 시나리오:**
```
사용자: "윤석열 대통령의 반도체 정책은 무엇인가요?"

AI: "윤석열 대통령은 반도체 산업을 '국가전략산업'으로 지정하고, 
2030년까지 세계 최고 수준의 경쟁력 확보를 목표로 하고 있습니다.

주요 정책은 다음과 같습니다:
1. 대규모 R&D 투자 확대
2. 인력 양성 프로그램 강화
3. 공급망 안정화 협력

📎 참고한 연설문:
- 2024년 3월 15일 - 반도체 클러스터 기공식 연설
- 2024년 6월 20일 - 과학기술 정책 발표
- 2024년 9월 5일 - 산업혁신 간담회"

[각 연설문 클릭 → 원문 확인 가능]
```

**주요 기능:**
- 자연어 질문 입력
- 실시간 스트리밍 답변 (타이핑 효과)
- 출처 제공 (연설문 링크 + 날짜)
- 대화 기록 유지 (멀티턴 대화)
- 카테고리 필터 (경제만, 외교만 등)

**기술 구현:**
- OpenAI text-embedding-3-small (임베딩)
- OpenAI GPT-4o-mini (답변 생성)
- Qdrant (벡터 DB)
- Server-Sent Events (SSE 스트리밍)

---

### 2. 연설문 전문 검색 ⭐⭐

**사용자 시나리오:**
```
검색창에 "경제 성장" 입력
→ 형태소 분석으로 "경제", "성장" 포함 연설문 검색
→ 날짜 필터, 카테고리 필터 적용 가능
→ 결과 목록에서 원문 확인
```

**주요 기능:**
- 키워드 검색 (Elasticsearch 형태소 분석)
- 날짜 범위 필터
- 카테고리 필터
- 페이지네이션
- 연설문 상세보기 (제목, 날짜, 행사명, 전문)

**기술 구현:**
- Elasticsearch 8.x
- Nori 형태소 분석기 (한국어)
- Spring Data Elasticsearch

---

### 3. 키워드 분석 대시보드 ⭐⭐

**대시보드 구성:**
```
┌─────────────────────────────────────┐
│  📊 귀띔 대시보드           │
├─────────────────────────────────────┤
│  [워드클라우드]                      │
│  AI ████ 반도체 ███ 경제 ██          │
│  기후 ██ 외교 █ ...                  │
├─────────────────────────────────────┤
│  [월별 연설 개수 추이]                │
│  라인 차트                            │
├─────────────────────────────────────┤
│  [카테고리별 분포]                    │
│  파이 차트: 경제 40%, 외교 25% ...   │
├─────────────────────────────────────┤
│  [키워드 트렌드]                      │
│  "AI" 선택 → 월별 언급 빈도 차트     │
└─────────────────────────────────────┘
```

**주요 기능:**
- TOP 20 키워드 워드클라우드
- 월별 연설 빈도 차트
- 카테고리별 파이 차트
- 특정 키워드 트렌드 분석
- 키워드 클릭 → 관련 연설문 바로 검색

**기술 구현:**
- Apache ECharts (차트 라이브러리)
- 형태소 분석 기반 키워드 추출
- Spring Batch (통계 집계)

---

### 4. 카테고리 자동 분류 ⭐

**카테고리:**
- 경제 (GDP, 수출, 일자리, 투자...)
- 외교 (동맹, 협력, 정상회담...)
- 복지 (민생, 복지, 의료, 교육...)
- 국방 (안보, 국방, 군사...)
- 환경 (기후, 탄소, 에너지...)
- 기타

**활용:**
- 챗봇에서 카테고리 선택 → 해당 분야만 검색
- 검색 필터
- 통계 분석

**기술 구현:**
- 키워드 기반 분류 로직 (Rule-based)
- Spring Batch (자동 분류 배치)

---

## 🏗️ 시스템 아키텍처

### 전체 구조도

```
┌──────────────┐
│   사용자     │
│  (반응형)    │
└──────┬───────┘
       │
┌──────▼────────────────────────────────┐
│     Vue 3 Frontend (shadcn-vue)       │
│  - 챗봇 UI (반응형)                   │
│  - 검색 UI (모바일 최적화)             │
│  - 대시보드 (차트, 터치 지원)          │
└──────┬────────────────────────────────┘
       │ REST API
┌──────▼────────────────────────────────┐
│     Spring Boot Backend               │
│                                        │
│  ┌─────────────────────────────────┐  │
│  │   RAG Service                   │  │
│  │  1. 질문 임베딩                  │  │
│  │  2. 벡터 검색 (Qdrant)          │  │
│  │  3. 컨텍스트 구성                │  │
│  │  4. GPT 호출 (OpenAI)           │  │
│  └─────────────────────────────────┘  │
│                                        │
│  ┌─────────────────────────────────┐  │
│  │   Search Service                │  │
│  │  - Elasticsearch 검색           │  │
│  └─────────────────────────────────┘  │
│                                        │
│  ┌─────────────────────────────────┐  │
│  │   Batch Service                 │  │
│  │  - MCP 데이터 수집              │  │
│  │  - 임베딩 생성                   │  │
│  │  - 키워드 추출                   │  │
│  │  - 카테고리 분류                 │  │
│  └─────────────────────────────────┘  │
└───────┬────────────────────────────────┘
        │
┌───────▼────────────────────────────────┐
│          Data Layer                    │
│                                         │
│  MySQL          Redis         ES       │
│  (메타데이터)    (캐시)       (검색)    │
│  + Flyway                               │
│                                         │
│  Qdrant         OpenAI API             │
│  (벡터 DB)      (임베딩+LLM)            │
└─────────────────────────────────────────┘
```

### 데이터 흐름

#### 1. 데이터 수집 & 전처리 (배치)
```
MCP 서버 (대통령 연설문 API)
    ↓
Spring Batch (매일 새벽 2시 실행)
    ↓
├─→ MySQL 저장 (원본 연설문 메타데이터)
├─→ Elasticsearch 인덱싱 (키워드 검색용)
├─→ 연설문 청킹 (500자 단위)
│    ↓
│   OpenAI Embedding API
│    ↓
└─→ Qdrant 저장 (벡터)
```

#### 2. 챗봇 질문 처리 (실시간)
```
사용자 질문: "반도체 정책은?"
    ↓
Backend RAG Service
    ↓
1. 질문 임베딩 (OpenAI)
    ↓
2. Qdrant 벡터 검색 → Top 5 유사 청크
    ↓
3. 프롬프트 구성
   - System: "연설문 전문가..."
   - Context: [검색된 5개 연설문]
   - Question: "반도체 정책은?"
    ↓
4. OpenAI GPT-4o-mini 호출 (스트리밍)
    ↓
5. Frontend로 SSE 스트리밍
    ↓
사용자 화면에 한 글자씩 출력
```

#### 3. 검색 처리
```
사용자 검색: "경제 성장"
    ↓
Elasticsearch (nori 형태소 분석)
    ↓
"경제" OR "성장" 포함 문서 검색
    ↓
결과 반환 (페이지네이션)
```

---

## 💾 데이터베이스 설계

### Flyway 마이그레이션 전략

**디렉토리 구조:**
```
src/main/resources/db/migration/
├── V1__init_schema.sql
├── V2__add_category_column.sql
├── V3__create_keywords_table.sql
├── V4__add_indexes.sql
└── V5__create_chat_tables.sql
```

**마이그레이션 파일 예시:**

#### V1__init_schema.sql
```sql
-- 연설문 테이블
CREATE TABLE speeches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(500) NOT NULL,
    content LONGTEXT NOT NULL,
    speech_date DATETIME NOT NULL,
    event_name VARCHAR(200),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_speech_date (speech_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 연설문 청크 테이블
CREATE TABLE speech_chunks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    speech_id BIGINT NOT NULL,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    vector_id VARCHAR(100),
    FOREIGN KEY (speech_id) REFERENCES speeches(id) ON DELETE CASCADE,
    INDEX idx_speech_id (speech_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### V2__add_category_column.sql
```sql
-- 카테고리 컬럼 추가
ALTER TABLE speeches 
ADD COLUMN category VARCHAR(50) AFTER event_name,
ADD INDEX idx_category (category);
```

#### V3__create_keywords_table.sql
```sql
-- 키워드 테이블
CREATE TABLE keywords (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(100) NOT NULL,
    frequency INT DEFAULT 1,
    month VARCHAR(7) NOT NULL,
    category VARCHAR(50),
    UNIQUE KEY uk_word_month_category (word, month, category),
    INDEX idx_month (month),
    INDEX idx_frequency (frequency DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### V4__create_chat_tables.sql
```sql
-- 채팅 세션 테이블
CREATE TABLE chat_sessions (
    id VARCHAR(100) PRIMARY KEY,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_active_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 채팅 메시지 테이블
CREATE TABLE chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE,
    INDEX idx_session_created (session_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 메시지 출처 테이블
CREATE TABLE message_sources (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id BIGINT NOT NULL,
    speech_id BIGINT NOT NULL,
    chunk_id BIGINT,
    relevance_score FLOAT,
    FOREIGN KEY (message_id) REFERENCES chat_messages(id) ON DELETE CASCADE,
    FOREIGN KEY (speech_id) REFERENCES speeches(id),
    INDEX idx_message_id (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### Flyway 설정 (application.yml)
```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    encoding: UTF-8
    validate-on-migrate: true
```

### MySQL 테이블 (최종 형태)

#### speeches (연설문)
```sql
CREATE TABLE speeches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(500) NOT NULL,
    content LONGTEXT NOT NULL,
    speech_date DATETIME NOT NULL,
    event_name VARCHAR(200),
    category VARCHAR(50),  -- ECONOMY, FOREIGN, WELFARE, DEFENSE, ENVIRONMENT, ETC
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_speech_date (speech_date),
    INDEX idx_category (category)
);
```

#### speech_chunks (연설문 청크)
```sql
CREATE TABLE speech_chunks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    speech_id BIGINT NOT NULL,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    vector_id VARCHAR(100),  -- Qdrant 벡터 ID
    FOREIGN KEY (speech_id) REFERENCES speeches(id) ON DELETE CASCADE,
    INDEX idx_speech_id (speech_id)
);
```

#### keywords (키워드)
```sql
CREATE TABLE keywords (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    word VARCHAR(100) NOT NULL,
    frequency INT DEFAULT 1,
    month VARCHAR(7) NOT NULL,  -- '2024-01'
    category VARCHAR(50),
    UNIQUE KEY uk_word_month_category (word, month, category),
    INDEX idx_month (month),
    INDEX idx_frequency (frequency DESC)
);
```

#### chat_sessions (챗봇 세션)
```sql
CREATE TABLE chat_sessions (
    id VARCHAR(100) PRIMARY KEY,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_active_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

#### chat_messages (챗봇 메시지)
```sql
CREATE TABLE chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,  -- 'user' or 'assistant'
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES chat_sessions(id) ON DELETE CASCADE,
    INDEX idx_session_created (session_id, created_at)
);
```

#### message_sources (챗봇 답변 출처)
```sql
CREATE TABLE message_sources (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id BIGINT NOT NULL,
    speech_id BIGINT NOT NULL,
    chunk_id BIGINT,
    relevance_score FLOAT,
    FOREIGN KEY (message_id) REFERENCES chat_messages(id) ON DELETE CASCADE,
    FOREIGN KEY (speech_id) REFERENCES speeches(id),
    INDEX idx_message_id (message_id)
);
```

### Elasticsearch 인덱스

#### speeches_index
```json
{
  "settings": {
    "analysis": {
      "tokenizer": {
        "nori_tokenizer": {
          "type": "nori_tokenizer",
          "decompound_mode": "mixed"
        }
      },
      "analyzer": {
        "korean": {
          "type": "custom",
          "tokenizer": "nori_tokenizer"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "id": { "type": "long" },
      "title": {
        "type": "text",
        "analyzer": "korean"
      },
      "content": {
        "type": "text",
        "analyzer": "korean"
      },
      "speech_date": { "type": "date" },
      "event_name": { "type": "keyword" },
      "category": { "type": "keyword" }
    }
  }
}
```

### Qdrant 컬렉션

#### speech_vectors
```json
{
  "vectors": {
    "size": 1536,  // OpenAI text-embedding-3-small 차원
    "distance": "Cosine"
  },
  "payload_schema": {
    "speech_id": "integer",
    "chunk_id": "integer",
    "content": "text",
    "category": "keyword",
    "speech_date": "datetime"
  }
}
```

---

## 🎨 화면 설계 (반응형 UI)

### 반응형 디자인 원칙

**브레이크포인트 (Tailwind CSS 기준):**
```
sm:  640px  (모바일 가로)
md:  768px  (태블릿)
lg:  1024px (데스크톱 작은 화면)
xl:  1280px (데스크톱 큰 화면)
2xl: 1536px (대형 모니터)
```

**모바일 우선 설계:**
- 기본 스타일 = 모바일
- md: 이상부터 태블릿/데스크톱 레이아웃 적용

**Linear 디자인 원칙 적용:**
- 미니멀한 인터페이스 (불필요한 요소 제거)
- 회색조 기반 + 포인트 컬러 (보라/파랑)
- 여백을 활용한 가독성
- 부드러운 전환 효과
- 다크모드 지원

### 1. 메인 페이지 (대시보드) - Linear 스타일

**디자인 컨셉:**
- Linear Insights 대시보드를 참고한 데이터 시각화
- 깔끔한 카드 레이아웃
- 미니멀한 색상 팔레트
- 반응형 그리드

**데스크톱 (lg 이상):**
```
┌─────────────────────────────────────────────────┐
│  귀띔            [검색] [챗봇] [통계]   │
├─────────────────────────────────────────────────┤
│                                                  │
│   🤖 AI에게 정책을 물어보세요                    │
│   ┌─────────────────────────────────────────┐  │
│   │ 질문을 입력하세요...           [전송]    │  │
│   └─────────────────────────────────────────┘  │
│                                                  │
│   ┌─────────────────┬───────────────────────┐  │
│   │  📊 키워드      │  📈 월별 연설 추이    │  │
│   │  [워드클라우드] │  [라인 차트]          │  │
│   └─────────────────┴───────────────────────┘  │
│                                                  │
│   🏷️ 카테고리별 분포                            │
│   ┌─────────────────────────────────────────┐  │
│   │      [파이 차트]                         │  │
│   └─────────────────────────────────────────┘  │
└─────────────────────────────────────────────────┘
```

**모바일 (<md):**
```
┌──────────────────┐
│ ☰  귀띔          │
├──────────────────┤
│                  │
│ 🤖 AI에게        │
│    정책 질문     │
│ ┌──────────────┐ │
│ │ 질문 입력... │ │
│ └──────────────┘ │
│                  │
│ 📊 주요 키워드   │
│ ┌──────────────┐ │
│ │ 워드클라우드 │ │
│ └──────────────┘ │
│                  │
│ 📈 월별 추이     │
│ ┌──────────────┐ │
│ │ 라인 차트    │ │
│ └──────────────┘ │
│                  │
│ 🏷️ 카테고리     │
│ ┌──────────────┐ │
│ │ 파이 차트    │ │
│ └──────────────┘ │
└──────────────────┘
```

**반응형 코드 예시 (Linear 스타일):**
```vue
<template>
  <div class="min-h-screen bg-background">
    <!-- 헤더 (Linear 스타일) -->
    <header class="sticky top-0 z-50 border-b border-border bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div class="container flex h-14 items-center">
        <button @click="toggleMobileMenu" class="mr-2 md:hidden p-2 hover:bg-surface rounded-md transition-colors">
          <Menu class="h-5 w-5" />
        </button>
        <h1 class="text-lg font-semibold tracking-tight">귀띔</h1>
        
        <!-- 데스크톱 네비게이션 -->
        <nav class="hidden md:flex ml-auto gap-1">
          <a href="#" class="px-3 py-2 text-sm text-muted-foreground hover:text-foreground hover:bg-surface rounded-md transition-colors">
            검색
          </a>
          <a href="#" class="px-3 py-2 text-sm text-muted-foreground hover:text-foreground hover:bg-surface rounded-md transition-colors">
            챗봇
          </a>
          <a href="#" class="px-3 py-2 text-sm text-muted-foreground hover:text-foreground hover:bg-surface rounded-md transition-colors">
            통계
          </a>
        </nav>
      </div>
    </header>

    <!-- 메인 컨텐츠 -->
    <main class="container py-6 md:py-8">
      <!-- Hero Section (Linear 스타일 검색창) -->
      <div class="mb-8 md:mb-12">
        <h2 class="text-2xl md:text-3xl font-semibold tracking-tight mb-2">
          🤖 AI에게 정책을 물어보세요
        </h2>
        <p class="text-sm text-muted-foreground mb-4">
          연설문 기반으로 신뢰할 수 있는 답변을 받아보세요
        </p>
        
        <div class="flex gap-2">
          <Input 
            class="flex-1 h-10 border-border bg-background hover:border-primary/50 focus-visible:ring-1 focus-visible:ring-primary transition-colors"
            placeholder="예: 윤석열 대통령의 반도체 정책은?"
          />
          <Button class="h-10 px-6 bg-primary hover:bg-primary/90 transition-colors">
            <Send class="h-4 w-4 mr-2" />
            질문하기
          </Button>
        </div>
      </div>

      <!-- 통계 요약 (Linear Insights 스타일) -->
      <div class="grid grid-cols-2 md:grid-cols-4 gap-3 md:gap-4 mb-6">
        <Card class="border border-border hover:border-primary/30 transition-colors">
          <CardContent class="p-4 md:p-6">
            <p class="text-xs text-muted-foreground mb-1">총 연설문</p>
            <p class="text-2xl md:text-3xl font-semibold tracking-tight">1,247</p>
            <div class="flex items-center gap-1 mt-2 text-xs text-success">
              <TrendingUp class="h-3 w-3" />
              <span>+12% 이번 달</span>
            </div>
          </CardContent>
        </Card>
        
        <Card class="border border-border hover:border-primary/30 transition-colors">
          <CardContent class="p-4 md:p-6">
            <p class="text-xs text-muted-foreground mb-1">주요 키워드</p>
            <p class="text-2xl md:text-3xl font-semibold tracking-tight">328</p>
            <div class="flex items-center gap-1 mt-2 text-xs text-muted-foreground">
              <Hash class="h-3 w-3" />
              <span>분석 완료</span>
            </div>
          </CardContent>
        </Card>
        
        <Card class="border border-border hover:border-primary/30 transition-colors">
          <CardContent class="p-4 md:p-6">
            <p class="text-xs text-muted-foreground mb-1">AI 질문</p>
            <p class="text-2xl md:text-3xl font-semibold tracking-tight">5,892</p>
            <div class="flex items-center gap-1 mt-2 text-xs text-success">
              <TrendingUp class="h-3 w-3" />
              <span>+28% 이번 주</span>
            </div>
          </CardContent>
        </Card>
        
        <Card class="border border-border hover:border-primary/30 transition-colors">
          <CardContent class="p-4 md:p-6">
            <p class="text-xs text-muted-foreground mb-1">평균 응답</p>
            <p class="text-2xl md:text-3xl font-semibold tracking-tight">2.8s</p>
            <div class="flex items-center gap-1 mt-2 text-xs text-success">
              <Zap class="h-3 w-3" />
              <span>빠름</span>
            </div>
          </CardContent>
        </Card>
      </div>

      <!-- 차트 그리드 (Linear Insights 스타일) -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6 mb-6">
        <!-- 워드클라우드 카드 -->
        <Card class="border border-border hover:border-primary/30 transition-colors">
          <CardHeader class="pb-3">
            <div class="flex items-center justify-between">
              <CardTitle class="text-sm font-medium text-muted-foreground">
                📊 주요 키워드
              </CardTitle>
              <Button variant="ghost" size="sm" class="h-8 px-2 text-xs">
                <Filter class="h-3 w-3 mr-1" />
                필터
              </Button>
            </div>
            <CardDescription class="text-xs">
              최근 6개월 기준
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div class="h-64 md:h-80 flex items-center justify-center">
              <!-- ECharts 워드클라우드 -->
              <div ref="wordCloudChart" class="w-full h-full"></div>
            </div>
          </CardContent>
        </Card>

        <!-- 라인 차트 카드 -->
        <Card class="border border-border hover:border-primary/30 transition-colors">
          <CardHeader class="pb-3">
            <div class="flex items-center justify-between">
              <CardTitle class="text-sm font-medium text-muted-foreground">
                📈 월별 연설 추이
              </CardTitle>
              <div class="flex gap-1">
                <Button variant="ghost" size="sm" class="h-8 px-2 text-xs">
                  6개월
                </Button>
                <Button variant="ghost" size="sm" class="h-8 px-2 text-xs text-muted-foreground">
                  1년
                </Button>
              </div>
            </div>
            <CardDescription class="text-xs">
              월별 연설문 개수
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div class="h-64 md:h-80">
              <!-- ECharts 라인 차트 -->
              <div ref="lineChart" class="w-full h-full"></div>
            </div>
          </CardContent>
        </Card>
      </div>

      <!-- 카테고리 분포 (전체 너비) -->
      <Card class="border border-border hover:border-primary/30 transition-colors">
        <CardHeader class="pb-3">
          <CardTitle class="text-sm font-medium text-muted-foreground">
            🏷️ 카테고리별 분포
          </CardTitle>
          <CardDescription class="text-xs">
            전체 연설문 분류 현황
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <!-- 파이 차트 -->
            <div class="h-64 md:h-80">
              <div ref="pieChart" class="w-full h-full"></div>
            </div>
            
            <!-- 범례 (Linear 스타일) -->
            <div class="space-y-3">
              <div class="flex items-center justify-between p-3 rounded-lg hover:bg-surface transition-colors cursor-pointer">
                <div class="flex items-center gap-3">
                  <div class="h-3 w-3 rounded-full bg-[#5E6AD2]"></div>
                  <span class="text-sm font-medium">경제</span>
                </div>
                <div class="text-right">
                  <p class="text-sm font-semibold">498</p>
                  <p class="text-xs text-muted-foreground">40%</p>
                </div>
              </div>
              
              <div class="flex items-center justify-between p-3 rounded-lg hover:bg-surface transition-colors cursor-pointer">
                <div class="flex items-center gap-3">
                  <div class="h-3 w-3 rounded-full bg-[#26B5CE]"></div>
                  <span class="text-sm font-medium">외교</span>
                </div>
                <div class="text-right">
                  <p class="text-sm font-semibold">312</p>
                  <p class="text-xs text-muted-foreground">25%</p>
                </div>
              </div>
              
              <div class="flex items-center justify-between p-3 rounded-lg hover:bg-surface transition-colors cursor-pointer">
                <div class="flex items-center gap-3">
                  <div class="h-3 w-3 rounded-full bg-[#8B5CF6]"></div>
                  <span class="text-sm font-medium">복지</span>
                </div>
                <div class="text-right">
                  <p class="text-sm font-semibold">249</p>
                  <p class="text-xs text-muted-foreground">20%</p>
                </div>
              </div>
              
              <div class="flex items-center justify-between p-3 rounded-lg hover:bg-surface transition-colors cursor-pointer">
                <div class="flex items-center gap-3">
                  <div class="h-3 w-3 rounded-full bg-[#EC4899]"></div>
                  <span class="text-sm font-medium">국방</span>
                </div>
                <div class="text-right">
                  <p class="text-sm font-semibold">125</p>
                  <p class="text-xs text-muted-foreground">10%</p>
                </div>
              </div>
              
              <div class="flex items-center justify-between p-3 rounded-lg hover:bg-surface transition-colors cursor-pointer">
                <div class="flex items-center gap-3">
                  <div class="h-3 w-3 rounded-full bg-[#10B981]"></div>
                  <span class="text-sm font-medium">환경</span>
                </div>
                <div class="text-right">
                  <p class="text-sm font-semibold">63</p>
                  <p class="text-xs text-muted-foreground">5%</p>
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'

const wordCloudChart = ref(null)
const lineChart = ref(null)
const pieChart = ref(null)

onMounted(() => {
  // Linear 스타일 차트 옵션
  const linearChartTheme = {
    backgroundColor: 'transparent',
    textStyle: {
      fontFamily: 'Inter, Pretendard, sans-serif',
      color: '#71717a' // muted-foreground
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    }
  }
  
  // 워드클라우드 초기화
  if (wordCloudChart.value) {
    const chart = echarts.init(wordCloudChart.value)
    chart.setOption({
      ...linearChartTheme,
      series: [{
        type: 'wordCloud',
        // ... 워드클라우드 옵션
      }]
    })
  }
  
  // 라인 차트 초기화 (Linear 스타일)
  if (lineChart.value) {
    const chart = echarts.init(lineChart.value)
    chart.setOption({
      ...linearChartTheme,
      xAxis: {
        type: 'category',
        boundaryGap: false,
        axisLine: { lineStyle: { color: '#e4e4e7' } }, // border
        axisLabel: { color: '#71717a' } // muted-foreground
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: '#e4e4e7', type: 'dashed' } }
      },
      series: [{
        type: 'line',
        smooth: true,
        lineStyle: { color: '#5E6AD2', width: 2 }, // Linear 보라색
        areaStyle: { 
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(94, 106, 210, 0.2)' },
            { offset: 1, color: 'rgba(94, 106, 210, 0)' }
          ])
        },
        itemStyle: { color: '#5E6AD2' }
      }]
    })
  }
})
</script>
```

**Linear 스타일 핵심 요소:**

1. **통계 카드**: 큰 숫자 + 작은 설명 + 트렌드 표시
2. **호버 효과**: 미묘한 border 색상 변화
3. **간격**: 적절한 여백 (p-4, gap-3)
4. **색상**: 회색조 + 포인트 컬러 (Linear 보라색 #5E6AD2)
5. **타이포그래피**: tracking-tight로 약간 좁은 자간

### 2. 챗봇 페이지

**데스크톱:**
```
┌─────────────────────────────────────────────────┐
│  ← 뒤로    정책 챗봇              [새 대화]      │
├─────────────────────────────────────────────────┤
│  카테고리: [전체▼] [경제] [외교] [복지] [국방]  │
├─────────────────────────────────────────────────┤
│                                                  │
│  👤 윤석열 대통령의 반도체 정책은?               │
│                                                  │
│  🤖 윤석열 대통령은 반도체 산업을                │
│     '국가전략산업'으로 지정하고...               │
│                                                  │
│     📎 참고한 연설문 (3개)                       │
│     ┌─────────────────────────────────────┐    │
│     │ 📄 2024-03-15 반도체 클러스터...    │    │
│     │ 📄 2024-06-20 과학기술 정책...      │    │
│     │ 📄 2024-09-05 산업혁신 간담회...    │    │
│     └─────────────────────────────────────┘    │
│                                                  │
│  👤 구체적으로 어떤 지원책이 있나요?             │
│                                                  │
│  🤖 [답변 타이핑 중...]                          │
│                                                  │
├─────────────────────────────────────────────────┤
│  [질문을 입력하세요...]              [전송] [🎤] │
└─────────────────────────────────────────────────┘
```

**모바일:**
```
┌──────────────────┐
│ ← 귀띔  [새대화] │
├──────────────────┤
│ [전체▼] [경제]   │
│ [외교] [복지]    │
├──────────────────┤
│                  │
│ 👤 반도체       │
│    정책은?       │
│                  │
│ 🤖 반도체 산업  │
│    국가전략...   │
│                  │
│ 📎 출처 (3)     │
│ ┌──────────────┐ │
│ │ 2024-03-15  │ │
│ │ 반도체...    │ │
│ └──────────────┘ │
│                  │
│ 👤 구체적으로?  │
│                  │
│ 🤖 [타이핑...]  │
│                  │
├──────────────────┤
│ [입력...]  [전송]│
└──────────────────┘
```

**반응형 코드 예시:**
```vue
<template>
  <div class="flex flex-col h-screen">
    <!-- 헤더 -->
    <header class="border-b p-4 flex items-center gap-2">
      <Button variant="ghost" size="icon" @click="goBack">
        <ChevronLeft class="h-5 w-5" />
      </Button>
      <h1 class="text-lg font-semibold flex-1">정책 챗봇</h1>
      <Button variant="outline" size="sm">새 대화</Button>
    </header>

    <!-- 카테고리 필터 -->
    <div class="border-b p-2 overflow-x-auto">
      <div class="flex gap-2 min-w-max">
        <Badge variant="secondary">전체</Badge>
        <Badge variant="outline">경제</Badge>
        <Badge variant="outline">외교</Badge>
        <Badge variant="outline">복지</Badge>
        <Badge variant="outline">국방</Badge>
      </div>
    </div>

    <!-- 메시지 영역 (스크롤) -->
    <div class="flex-1 overflow-y-auto p-4 space-y-4">
      <!-- 사용자 메시지 -->
      <div class="flex justify-end">
        <div class="max-w-[85%] md:max-w-[70%] bg-primary text-primary-foreground rounded-lg px-4 py-2">
          <p class="text-sm md:text-base">윤석열 대통령의 반도체 정책은?</p>
        </div>
      </div>

      <!-- AI 메시지 -->
      <div class="flex justify-start">
        <div class="max-w-[85%] md:max-w-[70%] bg-muted rounded-lg px-4 py-2">
          <p class="text-sm md:text-base mb-2">
            윤석열 대통령은 반도체 산업을 '국가전략산업'으로 지정하고...
          </p>
          
          <!-- 출처 카드 -->
          <Collapsible>
            <CollapsibleTrigger class="text-xs text-muted-foreground">
              📎 참고한 연설문 (3개)
            </CollapsibleTrigger>
            <CollapsibleContent class="mt-2 space-y-1">
              <div class="text-xs p-2 bg-background rounded border">
                📄 2024-03-15 반도체 클러스터 기공식
              </div>
              <div class="text-xs p-2 bg-background rounded border">
                📄 2024-06-20 과학기술 정책 발표
              </div>
              <div class="text-xs p-2 bg-background rounded border">
                📄 2024-09-05 산업혁신 간담회
              </div>
            </CollapsibleContent>
          </Collapsible>
        </div>
      </div>
    </div>

    <!-- 입력 영역 -->
    <div class="border-t p-4">
      <div class="flex gap-2">
        <Input
          v-model="message"
          placeholder="질문을 입력하세요..."
          class="flex-1"
          @keyup.enter="sendMessage"
        />
        <Button @click="sendMessage">
          <Send class="h-4 w-4" />
        </Button>
      </div>
    </div>
  </div>
</template>
```

### 3. 검색 페이지

**데스크톱:**
```
┌─────────────────────────────────────────────────┐
│  ← 뒤로    연설문 검색                           │
├─────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────┐    │
│  │ 경제 성장                    [🔍 검색]  │    │
│  └─────────────────────────────────────────┘    │
│                                                  │
│  필터: [2024년▼] [전체 카테고리▼]               │
│                                                  │
│  검색 결과 125개                                 │
│                                                  │
│  ┌─────────────────────────────────────────┐    │
│  │ 📄 2024년 경제정책 방향 연설             │    │
│  │ 2024-01-15 | 경제                        │    │
│  │ "경제 성장률 회복과 함께..."             │    │
│  └─────────────────────────────────────────┘    │
│                                                  │
│  ┌─────────────────────────────────────────┐    │
│  │ 📄 신년 기자회견                         │    │
│  │ 2024-01-02 | 경제                        │    │
│  │ "역동적 경제 성장을 위해..."             │    │
│  └─────────────────────────────────────────┘    │
│                                                  │
│  [1] 2 3 4 5 ... 13                              │
└─────────────────────────────────────────────────┘
```

**모바일:**
```
┌──────────────────┐
│ ← 연설문 검색    │
├──────────────────┤
│ ┌──────────────┐ │
│ │경제 성장 [🔍]│ │
│ └──────────────┘ │
│                  │
│ [2024▼] [전체▼] │
│                  │
│ 125개 결과       │
│                  │
│ ┌──────────────┐ │
│ │📄 경제정책   │ │
│ │2024-01-15    │ │
│ │경제          │ │
│ │"성장률..."   │ │
│ └──────────────┘ │
│                  │
│ ┌──────────────┐ │
│ │📄 기자회견   │ │
│ │2024-01-02    │ │
│ │경제          │ │
│ │"역동적..."   │ │
│ └──────────────┘ │
│                  │
│ [1] 2 3 ... 13   │
└──────────────────┘
```

### 4. 연설문 상세 페이지

**데스크톱:**
```
┌─────────────────────────────────────────────────┐
│  ← 뒤로                                          │
├─────────────────────────────────────────────────┤
│  2024년 경제정책 방향 연설                       │
│  📅 2024-01-15  |  🏷️ 경제                      │
│  📍 청와대 영빈관                                │
├─────────────────────────────────────────────────┤
│                                                  │
│  존경하는 국민 여러분,                           │
│                                                  │
│  오늘 저는 2024년 경제정책의 방향에 대해...      │
│                                                  │
│  [전체 연설문 내용]                              │
│                                                  │
├─────────────────────────────────────────────────┤
│  관련 키워드: #경제성장 #일자리 #투자 #혁신     │
│                                                  │
│  [이 연설문으로 AI에게 질문하기]                 │
└─────────────────────────────────────────────────┘
```

**모바일:**
```
┌──────────────────┐
│ ← 뒤로           │
├──────────────────┤
│ 경제정책 방향    │
│ 연설             │
│                  │
│ 📅 2024-01-15   │
│ 🏷️ 경제        │
│ 📍 청와대 영빈관│
├──────────────────┤
│                  │
│ 존경하는         │
│ 국민 여러분,     │
│                  │
│ 오늘 저는...     │
│                  │
│ [연설문 전체]    │
│                  │
├──────────────────┤
│ #경제성장        │
│ #일자리 #투자    │
│                  │
│ [AI에게 질문]    │
└──────────────────┘
```

---

## 🛠️ 기술 스택

### Backend
```
Framework: Spring Boot 3.2.x
Language: Java 17
Build Tool: Gradle 8.x

Database:
- MySQL 8.0 (연설문 메타데이터, 채팅 이력)
- Redis 7.x (캐싱)
- Elasticsearch 8.x (전문 검색)
- Qdrant 1.7.x (벡터 검색)

DB Migration:
- Flyway 10.x (스키마 버전 관리)

External API:
- OpenAI API
  - text-embedding-3-small (임베딩)
  - gpt-4o-mini (챗봇)

주요 라이브러리:
- Spring Data JPA
- Spring Batch
- Spring Data Elasticsearch
- Qdrant Java Client
- OpenAI Java SDK
- Lettuce (Redis Client)
- Flyway Core
```

### Frontend
```
Framework: Vue 3.4.x (Composition API)
Build Tool: Vite 5.x
Language: JavaScript

UI Library: shadcn-vue 0.10.x
- Radix Vue (Headless UI Components)
- Tailwind CSS 3.4.x (유틸리티 CSS)
- class-variance-authority (스타일 변형)

State Management: Pinia 2.x
Chart Library: Apache ECharts 5.x
HTTP Client: Axios 1.x

기타:
- Vue Router 4.x
- EventSource (SSE for Streaming)
- @vueuse/core (유틸리티)
- lucide-vue-next (아이콘)
```

### shadcn-vue 선택 이유

**Vuetify 대비 장점:**

1. **경량성**
   - Vuetify: ~500KB (번들 크기)
   - shadcn-vue: ~50KB (필요한 컴포넌트만)

2. **커스터마이징 자유도**
   - Headless UI 기반으로 스타일 완전 제어 가능
   - Tailwind CSS와 완벽한 통합
   - 디자인 시스템 확장 용이

3. **현대적인 디자인**
   - 미니멀하고 심플한 기본 스타일
   - Shadcn UI (React) 스타일 계승
   - 임팩트 있는 시각적 효과 (애니메이션, 전환)

4. **접근성**
   - Radix UI 기반으로 ARIA 표준 준수
   - 키보드 네비게이션 완벽 지원

5. **유연성**
   - 컴포넌트 소스 코드 복사 방식
   - 프로젝트에 직접 포함되어 수정 가능
   - 버전 충돌 없음

### 디자인 레퍼런스: Linear.app

**귀띔 프로젝트는 Linear의 디자인 철학과 UI를 참고합니다.**

Linear는 현대적인 B2B SaaS의 대표적인 디자인 사례로, 다음과 같은 특징이 있습니다:

**Linear의 핵심 디자인 원칙:**

1. **속도와 효율성 우선**
   - 빠른 로딩과 즉각적인 반응
   - 키보드 단축키 중심 UX
   - 불필요한 애니메이션 최소화

2. **미니멀리즘**
   - 군더더기 없는 깔끔한 인터페이스
   - 여백을 활용한 가독성
   - 단색 배경 + 포인트 컬러

3. **세련된 타이포그래피**
   - Inter 폰트 사용 (또는 Pretendard로 대체)
   - 명확한 계층 구조
   - 적절한 줄 간격과 크기 대비

4. **절제된 색상 팔레트**
   - 주로 회색조 기반
   - 상태별 컬러 (진행중: 파랑, 완료: 초록, 위험: 빨강)
   - 다크모드 지원

**귀띔 대시보드에 적용할 Linear 스타일:**

```css
/* 색상 팔레트 (Linear 스타일) */
:root {
  /* 배경 */
  --background: 0 0% 100%;           /* 흰색 */
  --surface: 0 0% 98%;               /* 밝은 회색 */
  
  /* 텍스트 */
  --foreground: 240 10% 3.9%;        /* 거의 검정 */
  --muted-foreground: 240 3.8% 46%;  /* 회색 */
  
  /* 보더 */
  --border: 240 5.9% 90%;            /* 연한 회색 */
  
  /* 액센트 (Linear 보라) */
  --primary: 262 83% 58%;            /* #5E6AD2 */
  --primary-foreground: 0 0% 100%;
  
  /* 상태 */
  --success: 142 76% 36%;            /* 초록 */
  --warning: 38 92% 50%;             /* 주황 */
  --danger: 0 84% 60%;               /* 빨강 */
}

/* 다크모드 */
.dark {
  --background: 240 10% 3.9%;
  --surface: 240 5% 6%;
  --foreground: 0 0% 98%;
  --muted-foreground: 240 5% 64.9%;
  --border: 240 3.7% 15.9%;
}
```

**대시보드 디자인 가이드:**

1. **카드 스타일 (Linear 스타일)**
   ```vue
   <Card class="border border-border bg-surface hover:border-primary/50 transition-colors">
     <CardHeader class="pb-3">
       <CardTitle class="text-sm font-medium text-muted-foreground">
         월별 연설 추이
       </CardTitle>
     </CardHeader>
     <CardContent>
       <!-- 차트 -->
     </CardContent>
   </Card>
   ```

2. **통계 숫자 표시 (Linear Insights 스타일)**
   ```vue
   <div class="space-y-1">
     <p class="text-2xl font-semibold">125</p>
     <p class="text-xs text-muted-foreground">총 연설문</p>
     <div class="flex items-center gap-1 text-xs text-success">
       <TrendingUp class="h-3 w-3" />
       <span>+12%</span>
     </div>
   </div>
   ```

3. **테이블 스타일 (Linear Issue List 스타일)**
   ```vue
   <Table>
     <TableHeader>
       <TableRow class="border-b border-border hover:bg-transparent">
         <TableHead class="text-xs font-medium text-muted-foreground">
           제목
         </TableHead>
         <TableHead>날짜</TableHead>
         <TableHead>카테고리</TableHead>
       </TableRow>
     </TableHeader>
     <TableBody>
       <TableRow class="border-b border-border hover:bg-surface/50 cursor-pointer">
         <TableCell class="font-medium">경제정책 방향</TableCell>
         <TableCell class="text-muted-foreground">2024-01-15</TableCell>
         <TableCell>
           <Badge variant="secondary">경제</Badge>
         </TableCell>
       </TableRow>
     </TableBody>
   </Table>
   ```

4. **차트 스타일 가이드**
   - 배경: 투명 또는 surface 컬러
   - 그리드: 연한 회색 (--border)
   - 데이터 포인트: primary 컬러
   - 툴팁: 다크 배경 + 흰 글씨
   - 애니메이션: ease-in-out, 300ms

**Linear에서 영감받은 인터랙션:**

1. **호버 효과**
   - 카드: border 색상 변경 (`hover:border-primary/50`)
   - 버튼: 배경 살짝 어두워짐 (`hover:bg-primary/90`)
   - 리스트 아이템: 배경 살짝 밝아짐 (`hover:bg-surface/50`)

2. **포커스 스타일**
   - 2px 두께 ring
   - primary 컬러 또는 포커스 전용 컬러
   - `focus-visible:ring-2 focus-visible:ring-primary`

3. **트랜지션**
   - 모든 색상 변화: `transition-colors duration-150`
   - 레이아웃 변화: `transition-all duration-200 ease-in-out`
   - 과도한 애니메이션 지양

4. **로딩 상태**
   - 스켈레톤 UI (회색 배경 + shimmer 효과)
   - 스피너는 최소화, 대신 프로그레스 바 선호

**타이포그래피 (Linear 스타일):**

```css
/* 제목 */
.heading-1 { @apply text-2xl font-semibold tracking-tight; }
.heading-2 { @apply text-xl font-semibold tracking-tight; }
.heading-3 { @apply text-lg font-semibold; }

/* 본문 */
.body { @apply text-sm leading-relaxed; }
.body-small { @apply text-xs leading-normal; }

/* 라벨 */
.label { @apply text-xs font-medium uppercase tracking-wide text-muted-foreground; }
```

**참고 스크린샷:**
- Linear Insights 대시보드: 데이터 시각화 참고
- Linear Projects: 프로젝트 카드 레이아웃 참고
- Linear Issues: 리스트/테이블 스타일 참고

이를 통해 **전문적이고 세련된 B2B SaaS 느낌**의 대시보드를 구현합니다.

**shadcn-vue 컴포넌트 예시:**
```vue
<!-- 버튼 컴포넌트 -->
<Button variant="default" size="lg">
  전송
</Button>

<!-- 카드 컴포넌트 -->
<Card>
  <CardHeader>
    <CardTitle>주요 키워드</CardTitle>
    <CardDescription>최근 6개월 기준</CardDescription>
  </CardHeader>
  <CardContent>
    <!-- 내용 -->
  </CardContent>
  <CardFooter>
    <!-- 푸터 -->
  </CardFooter>
</Card>

<!-- 대화상자 -->
<Dialog>
  <DialogTrigger>열기</DialogTrigger>
  <DialogContent>
    <DialogHeader>
      <DialogTitle>연설문 상세</DialogTitle>
    </DialogHeader>
    <!-- 내용 -->
  </DialogContent>
</Dialog>
```

### Infrastructure
```
Containerization: Docker & Docker Compose
Version Control: Git & GitHub
CI/CD: GitHub Actions (선택 사항)
Monitoring: Spring Actuator + Prometheus (선택 사항)
```

---

## 📅 개발 일정 (8주)

### Week 1: 프로젝트 셋업 & 기본 데이터 수집
**목표: MCP 연동 → DB 저장 → 화면 출력**

**Backend**
- Spring Boot 프로젝트 생성 (Gradle, Java 17)
- MySQL Docker Compose 작성
- **Flyway 설정 및 V1 마이그레이션 작성**
- MCP 서버 연동 테스트 (대통령 연설문 조회)
- Entity 설계 (Speech 테이블)
- 기본 CRUD API (`GET /api/speeches`, `GET /api/speeches/{id}`)
- Spring Batch 기본 구성 (연설문 수집 Job)

**Frontend**
- Vue 3 + Vite 프로젝트 생성
- **shadcn-vue 설치 및 설정**
- **Tailwind CSS 설정**
- 기본 레이아웃 (헤더, 사이드바)
- 연설문 목록 페이지
- **모바일 메뉴 구현 (햄버거 메뉴)**

**완료 기준 (데모)**
- ✅ 웹 화면에 연설문 50개가 테이블로 표시됨 (제목, 날짜, 행사명)
- ✅ 배치 수동 실행 → DB에 데이터 저장 확인
- ✅ **모바일/태블릿/데스크톱에서 레이아웃 정상 작동**
- ✅ **Flyway 마이그레이션 실행 확인**

---

### Week 2: Elasticsearch & 전문 검색
**목표: 키워드 검색 기능 완성**

**Backend**
- Elasticsearch Docker 추가
- 한국어 분석기 설정 (nori plugin)
- Speech 인덱스 매핑 정의
- 배치에서 ES 동기화 (MySQL → Elasticsearch)
- 검색 API (`GET /api/speeches/search?keyword=경제&page=0`)
- Pagination 적용

**Frontend**
- 검색 페이지 생성
- **반응형 검색창 (모바일: 전체 너비, 데스크톱: 중앙 정렬)**
- 검색 결과 리스트 (카드 형태)
- **반응형 필터 (모바일: 드롭다운, 데스크톱: 칩)**
- Pagination 컴포넌트

**완료 기준 (데모)**
- ✅ "경제 성장" 검색 → 형태소 분석되어 관련 연설문 검색됨
- ✅ 날짜 필터 (2024년만) 적용하여 검색
- ✅ **모바일에서 필터가 화면을 가리지 않고 사용 가능**

---

### Week 3: 벡터 DB 구축 & 임베딩
**목표: RAG를 위한 벡터 검색 인프라 완성**

**Backend**
- Qdrant Docker 추가
- OpenAI API 키 설정 (환경변수)
- 임베딩 서비스 구현 (OpenAI text-embedding-3-small)
- 연설문 청킹 로직 (500자 단위, 100자 오버랩)
- 배치 Job: 연설문 → 청크 → 임베딩 → Qdrant 저장
- 벡터 검색 서비스 구현

**Frontend**
- 로딩 상태 관리 (Pinia)
- **shadcn-vue Skeleton 컴포넌트 (로딩 UI)**
- **Toast 알림 (에러 핸들링)**

**완료 기준 (데모)**
- ✅ 배치 실행 → 연설문 100개가 벡터화되어 Qdrant에 저장됨
- ✅ Postman으로 벡터 검색 테스트 → 유사한 연설문 청크 5개 반환

---

### Week 4: RAG 파이프라인 & 챗봇 API
**목표: 질문 → 문서 검색 → GPT 답변 생성**

**Backend**
- OpenAI GPT-4o-mini API 연동
- RAG 서비스 구현
    - 질문 임베딩
    - Qdrant 벡터 검색 (Top 5)
    - 프롬프트 구성
    - GPT 호출
    - 출처와 함께 반환
- 프롬프트 템플릿 작성
- 챗봇 API (`POST /api/chat`)
- 대화 히스토리 저장 (Session 테이블)
- **Flyway V3 마이그레이션 (채팅 테이블)**

**Frontend**
- 챗봇 페이지 생성
- **반응형 채팅 UI (모바일: 전체 화면, 데스크톱: 중앙 정렬)**
- **메시지 버블 (모바일: 80% 너비, 데스크톱: 70%)**
- **Collapsible 출처 카드 (shadcn-vue)**
- 입력창 + 전송 버튼

**완료 기준 (데모)**
- ✅ "윤석열 대통령의 반도체 정책은?" 입력 → GPT가 관련 연설문 기반 답변 생성
- ✅ 답변 하단에 "참고 연설문 3개" 카드 표시 → 클릭하면 원문 확인
- ✅ **모바일에서 입력창이 키보드에 가려지지 않음**

---

### Week 5: 챗봇 고도화 & 스트리밍
**목표: 실시간 답변 + 멀티턴 대화**

**Backend**
- 대화 컨텍스트 관리 (최근 3턴 기억)
- SSE 스트리밍 API 구현
- Redis 캐싱 (동일 질문 임베딩 결과)

**Frontend**
- 스트리밍 답변 구현 (EventSource API)
- **타이핑 효과 애니메이션 (CSS)**
- 세션 관리 (새 대화 시작 버튼)
- **슬라이드 인 대화 히스토리 (모바일: Sheet, 데스크톱: Sidebar)**

**완료 기준 (데모)**
- ✅ 사용자: "경제 정책은?" → AI: "윤석열 대통령은..." (한 글자씩 출력)
- ✅ 사용자: "더 자세히 알려줘" (컨텍스트 기억) → AI: "앞서 말씀드린 경제 정책은..."
- ✅ 동일 질문 2번 입력 → 두 번째는 즉시 응답 (캐시)
- ✅ **모바일에서 타이핑 애니메이션 부드러움**

---

### Week 6: 키워드 분석 & 대시보드
**목표: 연설문 통계 시각화**

**Backend**
- 키워드 추출 배치 (형태소 분석 기반)
- **Flyway V4 마이그레이션 (키워드 테이블)**
- 통계 API
    - `GET /api/stats/keywords/top?limit=20`
    - `GET /api/stats/keywords/trend?keyword=AI&from=2024-01`
    - `GET /api/stats/speeches/count/monthly`

**Frontend**
- Apache ECharts 설치
- 대시보드 페이지
    - **반응형 그리드 (모바일: 1열, 태블릿: 2열)**
    - 워드클라우드 (TOP 20 키워드)
    - 라인 차트 (월별 연설 개수)
    - 키워드 트렌드 차트
- **터치 제스처 지원 (차트 확대/축소)**
- 키워드 클릭 → 관련 연설문 검색

**완료 기준 (데모)**
- ✅ 대시보드에 "최근 1년 TOP 20 키워드" 워드클라우드 표시
- ✅ "AI" 클릭 → 월별 언급 빈도 라인 차트 + 관련 연설문 목록
- ✅ **모바일에서 차트가 화면에 맞게 축소됨**

---

### Week 7: 카테고리 분류 & 필터 강화
**목표: 연설문 자동 분류 + 챗봇 필터 적용**

**Backend**
- 카테고리 Enum (경제, 외교, 복지, 국방, 환경, 기타)
- **Flyway V5 마이그레이션 (카테고리 컬럼)**
- 키워드 기반 분류 로직
- 배치에서 기존 데이터 분류
- 카테고리별 검색 API
- 챗봇에 카테고리 필터 적용

**Frontend**
- **반응형 카테고리 필터 (모바일: 가로 스크롤 칩, 데스크톱: 전체 표시)**
- 카테고리별 파이 차트
- 챗봇에서 카테고리 선택 옵션

**완료 기준 (데모)**
- ✅ 챗봇에서 "경제" 카테고리 선택 → "반도체 정책은?" 질문 → 경제 관련 연설문만 참고하여 답변
- ✅ 파이 차트로 카테고리 분포 확인 (경제 40%, 외교 25%...)
- ✅ **모바일에서 카테고리 칩이 가로 스크롤됨**

---

### Week 8: 성능 최적화 & 배포
**목표: 프로덕션 준비 완료**

**Backend**
- Redis 캐싱 전략 최종 정리
    - 검색 결과: 5분 TTL
    - 통계 데이터: 1시간 TTL
    - 임베딩: 24시간 TTL
- Elasticsearch 쿼리 최적화
- API 응답 시간 모니터링 (Spring Actuator)
- Docker Compose 전체 스택 구성
- 환경변수 관리 (.env.example 제공)
- Health check endpoint
- 에러 로깅 (Logback)
- **Flyway 마이그레이션 검증**

**Frontend**
- **반응형 로딩 스켈레톤 (모든 페이지)**
- 에러 바운더리
- **모바일 반응형 최적화 최종 점검**
    - [ ] iPhone SE (375px)
    - [ ] iPhone 14 (390px)
    - [ ] iPad (768px)
    - [ ] Desktop (1280px)
- **터치 타겟 크기 검증 (최소 44x44px)**
- PWA 기본 설정 (manifest.json)
- 챗봇 통계 페이지
    - 총 질문 수
    - 평균 응답 시간
    - 인기 질문 TOP 5

**완료 기준 (데모)**
- ✅ `docker-compose up` 한 번에 전체 앱 실행
- ✅ 챗봇 응답 시간 평균 3초 이내
- ✅ **모바일 화면에서도 모든 기능 정상 작동**
- ✅ 통계 페이지에서 "오늘 50개 질문, 평균 2.8초 응답" 확인
- ✅ **Lighthouse 모바일 점수 90+ (Performance, Accessibility)**

---

## 💰 예상 비용

### OpenAI API 비용 (개발 8주)

**임베딩 (text-embedding-3-small)**
```
- 연설문 1,000개 × 평균 2,000토큰 = 2M 토큰
- 요금: $0.02 / 1M 토큰
- 비용: 약 $0.04
```

**챗봇 (gpt-4o-mini)**
```
- 테스트 500회 × 평균 3,000토큰 (컨텍스트 포함) = 1.5M 토큰
- 요금: $0.15 / 1M 토큰
- 비용: 약 $0.23
```

**총 개발 비용: 약 $0.30 (8주간)**

### 인프라 비용 (로컬 개발)
```
- Docker Desktop: 무료
- MySQL, Redis, Elasticsearch, Qdrant: 무료 (Docker 이미지)

총: $0
```

### 권장 사항
- OpenAI API 크레딧 $5 충전 권장 (여유분 포함)
- 실제 사용량은 $1 미만일 가능성 높음

---

## 🎯 포트폴리오 어필 포인트

### 1. 최신 AI 기술 활용
- ✅ RAG (Retrieval-Augmented Generation) 구현
- ✅ 벡터 임베딩 & 시맨틱 검색
- ✅ LLM 프롬프트 엔지니어링
- ✅ 실시간 스트리밍 응답

### 2. 복잡한 아키텍처 설계
- ✅ 4개 데이터베이스 조합 (MySQL, Redis, Elasticsearch, Qdrant)
- ✅ 이벤트 드리븐 아키텍처 (배치 처리)
- ✅ 3단계 캐싱 전략
- ✅ 마이크로서비스 고려 레이어 분리
- ✅ **Flyway를 통한 체계적인 DB 버전 관리**

### 3. 실무 역량 증명
- ✅ Spring Batch (데이터 파이프라인)
- ✅ 성능 최적화 (쿼리 최적화, 캐싱)
- ✅ 외부 API 연동 (OpenAI, MCP)
- ✅ Docker 기반 인프라 구성
- ✅ **DB 마이그레이션 전략 (Flyway)**
- ✅ **모던 프론트엔드 스택 (shadcn-vue + Tailwind)**

### 4. 실용적 가치
- ✅ 실제 사용 가능한 서비스
- ✅ 명확한 사용자 가치 제공
- ✅ 차별화된 도메인 (정책 분석)
- ✅ 신뢰할 수 있는 출처 기반 답변
- ✅ **모바일/태블릿/데스크톱 완벽 지원**

---

## 🚀 성공 기준

### 기술적 목표
- [ ] 챗봇 응답 시간 평균 3초 이내
- [ ] 검색 응답 시간 500ms 이내
- [ ] 벡터 검색 정확도 (Top 5 유사도 0.7 이상)
- [ ] 배치 처리 완료 시간 10분 이내 (1,000개 기준)
- [ ] **Flyway 마이그레이션 무중단 실행**
- [ ] **Lighthouse 점수 90+ (모바일)**

### 기능적 목표
- [ ] 연설문 데이터 1,000개 이상 수집
- [ ] 챗봇 질문 정확도 80% 이상 (출처 제공)
- [ ] 키워드 100개 이상 추출
- [ ] 카테고리 분류 정확도 70% 이상

### 사용자 경험 목표
- [ ] 모바일 반응형 지원 (iPhone SE ~ Desktop)
- [ ] 로딩 시간 3초 이내 (초기 화면)
- [ ] 직관적인 UI (테스트 사용자 5명 이상)
- [ ] **터치 타겟 최소 44x44px (접근성)**
- [ ] **모바일 키보드 충돌 없음**

---

## 🔐 인증/세션 관리

### 회원가입 없음 (세션 기반)
**현재 기획안에는 회원가입/로그인 기능이 포함되지 않습니다.**

**대신 세션 기반 대화 이력 관리:**
```javascript
// Frontend (localStorage)
const sessionId = localStorage.getItem('policytracker_session') 
                  || crypto.randomUUID();
localStorage.setItem('policytracker_session', sessionId);

// Backend
// sessionId로 chat_sessions 테이블 조회/저장
// 30일 이상 미사용 세션은 배치로 삭제
```

**사용량 제한 (선택 사항):**
```java
// Redis에 세션별 사용량 카운트
// 하루 50회 제한 등
String key = "usage:" + sessionId + ":" + LocalDate.now();
Long count = redisTemplate.opsForValue().increment(key);
if (count > 50) {
    throw new UsageLimitException("일일 사용량을 초과했습니다.");
}
```

**향후 확장 가능:**
- 회원가입/로그인 추가 (대화 이력 영구 저장)
- 즐겨찾기 기능
- 맞춤형 추천

---

## 📝 다음 단계

### 즉시 시작 가능 항목
1. GitHub 저장소 생성
2. OpenAI API 키 발급 ($5 크레딧)
3. Docker Desktop 설치
4. Spring Boot 프로젝트 생성
5. Vue 프로젝트 생성
6. **Flyway 초기 설정**
7. **shadcn-vue 설치**

### 1주차 준비 사항
- MCP 서버 URL 확인
- MySQL 8.0 Docker 이미지 다운로드
- IDE 설정 (IntelliJ IDEA + VS Code)
- **Tailwind CSS 익스텐션 설치 (VS Code)**

---

## 📚 참고 자료

### 공식 문서
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Vue 3 Documentation](https://vuejs.org/)
- [shadcn-vue Documentation](https://www.shadcn-vue.com/)
- [Tailwind CSS Documentation](https://tailwindcss.com/)
- [OpenAI API Documentation](https://platform.openai.com/docs)
- [Qdrant Documentation](https://qdrant.tech/documentation/)
- [Elasticsearch Documentation](https://www.elastic.co/guide/index.html)

### 디자인 레퍼런스
- **[Linear.app](https://linear.app/)** - 대시보드 및 전체 UI 디자인 참고
  - Linear Insights: 데이터 시각화 스타일
  - Linear Projects: 카드 레이아웃
  - Linear Issues: 리스트/테이블 디자인
- [Linear Design System](https://linear.app/readme) - 디자인 원칙
- [Inter Font](https://rsms.me/inter/) - Linear에서 사용하는 타이포그래피

### 참고 GitHub 레포지토리
- [MCP 서버 - data-go-mcp-servers](https://github.com/Koomook/data-go-mcp-servers)
- [shadcn-vue](https://github.com/radix-vue/shadcn-vue)

---

## 🆕 v2.0 변경사항 요약

### 1. Flyway DB 버전 관리 추가
- 모든 테이블 생성을 마이그레이션 스크립트로 관리
- 버전별 스키마 변경 추적 가능
- 팀 협업 시 DB 동기화 용이

### 2. UI 라이브러리 변경: Vuetify → shadcn-vue
- 경량화 (500KB → 50KB)
- Headless UI 기반 완벽한 커스터마이징
- Tailwind CSS와 시너지
- 모던하고 임팩트 있는 디자인

### 3. 반응형 UI 전략 구체화
- 모바일 우선 설계 원칙
- Tailwind 브레이크포인트 활용
- 모든 화면별 반응형 레이아웃 예시 제공
- 터치 타겟 크기, 키보드 충돌 등 UX 세부사항 반영

### 4. Linear 디자인 시스템 참고 ⭐ NEW
- Linear.app의 디자인 철학과 UI를 벤치마크
- 미니멀리즘, 속도, 효율성 중심의 디자인
- 세련된 색상 팔레트 (회색조 + Linear 보라색)
- 대시보드, 카드, 테이블 등 모든 컴포넌트에 Linear 스타일 적용
- 구체적인 CSS 코드 및 컴포넌트 예시 제공

---

**작성일:** 2026년 2월 10일  
**버전:** 2.0  
**작성자:** PolicyTracker 개발팀  
**주요 업데이트:** Flyway, shadcn-vue, 반응형 UI