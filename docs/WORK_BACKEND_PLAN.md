# 귀띔 (Guitteum) 백엔드 개발 작업 계획서

> AI 기반 대통령 연설문 분석 플랫폼 — Backend
> 개발 기간: 8주 (2개월)
> 작성일: 2026-02-10

---

## 1. 프로젝트 개요

대통령 연설문을 수집·분석하여 RAG 기반 정책 챗봇, 전문 검색, 키워드 대시보드를 제공하는 웹 서비스.

**핵심 기능 (우선순위):**

| 순위 | 기능 | 설명 |
|------|------|------|
| 1 | RAG 정책 챗봇 | 연설문 기반 AI 질의응답 + SSE 스트리밍 |
| 2 | 연설문 전문 검색 | Elasticsearch + Nori 형태소 분석 |
| 3 | 키워드 대시보드 | 워드클라우드, 트렌드 차트 |
| 4 | 카테고리 자동 분류 | 키워드 기반 Rule-based 분류 |

---

## 2. 기술 스택

| 항목 | 기술 |
|------|------|
| Framework | Spring Boot 3.2.x |
| Language | Java 17 |
| Build Tool | Gradle 8.x |
| RDBMS | MySQL 8.0 |
| Cache | Redis 7.x |
| 전문 검색 | Elasticsearch 8.x + Nori |
| 벡터 DB | Qdrant 1.7.x |
| DB 마이그레이션 | Flyway 10.x |
| 배치 | Spring Batch |
| 외부 API | OpenAI (text-embedding-3-small, GPT-4o-mini) |

---

## 3. 패키지 구조

```
src/main/java/com/guitteum/
├── GuitteumApplication.java
│
├── api/                          # REST 컨트롤러 계층
│   ├── chat/
│   │   └── ChatController.java
│   ├── speech/
│   │   └── SpeechController.java
│   ├── search/
│   │   └── SearchController.java
│   └── stats/
│       └── StatsController.java
│
├── domain/                       # 비즈니스 로직 계층
│   ├── chat/
│   │   ├── entity/
│   │   │   ├── ChatSession.java
│   │   │   ├── ChatMessage.java
│   │   │   └── MessageSource.java
│   │   ├── repository/
│   │   │   ├── ChatSessionRepository.java
│   │   │   └── ChatMessageRepository.java
│   │   ├── service/
│   │   │   ├── ChatService.java
│   │   │   └── RagService.java
│   │   └── dto/
│   │       ├── ChatRequest.java
│   │       └── ChatResponse.java
│   │
│   ├── speech/
│   │   ├── entity/
│   │   │   ├── Speech.java
│   │   │   └── SpeechChunk.java
│   │   ├── repository/
│   │   │   ├── SpeechRepository.java
│   │   │   └── SpeechChunkRepository.java
│   │   ├── service/
│   │   │   └── SpeechService.java
│   │   └── dto/
│   │       ├── SpeechResponse.java
│   │       └── SpeechDetailResponse.java
│   │
│   └── keyword/
│       ├── entity/
│       │   └── Keyword.java
│       ├── repository/
│       │   └── KeywordRepository.java
│       ├── service/
│       │   └── KeywordService.java
│       └── dto/
│           └── KeywordResponse.java
│
├── infra/                        # 외부 인프라 연동 계층
│   ├── openai/
│   │   ├── OpenAiClient.java
│   │   └── OpenAiConfig.java
│   ├── qdrant/
│   │   ├── QdrantClient.java
│   │   └── QdrantConfig.java
│   ├── elasticsearch/
│   │   ├── SpeechDocument.java
│   │   ├── SpeechSearchRepository.java
│   │   └── ElasticsearchConfig.java
│   └── redis/
│       └── RedisConfig.java
│
├── batch/                        # Spring Batch 계층
│   ├── job/
│   │   ├── SpeechCollectJobConfig.java
│   │   ├── EmbeddingJobConfig.java
│   │   ├── KeywordExtractJobConfig.java
│   │   └── CategoryClassifyJobConfig.java
│   ├── reader/
│   ├── processor/
│   └── writer/
│
└── global/                       # 공통 설정
    ├── config/
    │   ├── WebConfig.java
    │   └── BatchConfig.java
    ├── exception/
    │   ├── GlobalExceptionHandler.java
    │   └── ErrorResponse.java
    └── common/
        └── Category.java         # enum: ECONOMY, FOREIGN, WELFARE, DEFENSE, ENVIRONMENT, ETC
```

```
src/main/resources/
├── application.yml
├── application-local.yml
├── application-prod.yml
└── db/migration/
    ├── V1__init_schema.sql
    ├── V2__add_category_column.sql
    ├── V3__create_keywords_table.sql
    ├── V4__create_chat_tables.sql
    └── V5__add_indexes.sql
```

---

## 4. Docker Compose 구성

```yaml
# docker-compose.yml 구성 계획
services:
  mysql:        # MySQL 8.0  (포트 3306)
  redis:        # Redis 7.x  (포트 6379)
  elasticsearch: # ES 8.x + Nori  (포트 9200)
  qdrant:       # Qdrant 1.7.x  (포트 6333)
  backend:      # Spring Boot 앱  (포트 8080) - depends_on: 위 4개 서비스
```

**볼륨 구성:**
- `mysql-data` : MySQL 영속 데이터
- `es-data` : Elasticsearch 인덱스 데이터
- `qdrant-data` : Qdrant 벡터 데이터
- `redis-data` : Redis 데이터

---

## 5. Flyway 마이그레이션 계획

| 파일명 | 적용 주차 | 설명 |
|--------|----------|------|
| `V1__init_schema.sql` | Week 1 | speeches, speech_chunks 테이블 생성 |
| `V2__add_category_column.sql` | Week 7 | speeches에 category 컬럼 추가 |
| `V3__create_keywords_table.sql` | Week 6 | keywords 테이블 생성 |
| `V4__create_chat_tables.sql` | Week 4 | chat_sessions, chat_messages, message_sources 생성 |
| `V5__add_indexes.sql` | Week 8 | 성능 최적화 인덱스 추가 |

---

## 6. API 명세

### 연설문 API
| Method | Endpoint | 설명 | 주차 |
|--------|----------|------|------|
| GET | `/api/speeches` | 연설문 목록 (페이지네이션) | W1 |
| GET | `/api/speeches/{id}` | 연설문 상세 조회 | W1 |

### 검색 API
| Method | Endpoint | 설명 | 주차 |
|--------|----------|------|------|
| GET | `/api/speeches/search` | 키워드 검색 (query, category, dateFrom, dateTo, page, size) | W2 |

### 챗봇 API
| Method | Endpoint | 설명 | 주차 |
|--------|----------|------|------|
| POST | `/api/chat` | 질문 → AI 답변 (JSON) | W4 |
| POST | `/api/chat/stream` | 질문 → SSE 스트리밍 답변 | W5 |
| GET | `/api/chat/sessions/{sessionId}/messages` | 대화 이력 조회 | W5 |
| DELETE | `/api/chat/sessions/{sessionId}` | 대화 세션 삭제 | W5 |

### 통계 API
| Method | Endpoint | 설명 | 주차 |
|--------|----------|------|------|
| GET | `/api/stats/keywords/top` | TOP N 키워드 (limit) | W6 |
| GET | `/api/stats/keywords/trend` | 키워드 월별 트렌드 (keyword, from, to) | W6 |
| GET | `/api/stats/speeches/monthly` | 월별 연설문 개수 | W6 |
| GET | `/api/stats/speeches/category` | 카테고리별 분포 | W7 |
| GET | `/api/stats/summary` | 요약 통계 (총 연설문 수, 키워드 수 등) | W6 |

### 배치 실행 API (개발용)
| Method | Endpoint | 설명 | 주차 |
|--------|----------|------|------|
| POST | `/api/batch/collect` | 연설문 수집 배치 수동 실행 | W1 |
| POST | `/api/batch/embed` | 임베딩 배치 수동 실행 | W3 |
| POST | `/api/batch/keywords` | 키워드 추출 배치 수동 실행 | W6 |
| POST | `/api/batch/classify` | 카테고리 분류 배치 수동 실행 | W7 |

---

## 7. 주차별 상세 작업 계획

---

### Week 1: 프로젝트 셋업 & 기본 데이터 수집

> 목표: 프로젝트 초기화 → MCP 연동 → DB 저장 → 연설문 목록 API

- [x] Spring Boot 프로젝트 생성 (Spring Initializr)
  - Spring Web, Spring Data JPA, MySQL Driver, Flyway, Validation, Actuator
  - `build.gradle` 의존성 정의
  - `settings.gradle` 작성
- [x] `.gitignore` 작성 (Java/Gradle/IntelliJ 대상)
- [x] `docker-compose.yml` 작성 (MySQL 8.0 단독 실행)
- [x] `application.yml` / `application-local.yml` 설정
  - MySQL 접속 정보
  - Flyway 설정
  - 서버 포트, 로깅 레벨
- [x] Flyway `V1__init_schema.sql` 작성
  - `speeches` 테이블 (id, title, content, speech_date, event_name, created_at, updated_at)
  - `speech_chunks` 테이블 (id, speech_id, chunk_index, content, vector_id)
- [x] `Speech` Entity + `SpeechRepository` 작성
- [x] `SpeechService` 작성 (목록 조회, 상세 조회)
- [x] `SpeechController` 작성
  - `GET /api/speeches` (Pageable)
  - `GET /api/speeches/{id}`
- [x] `SpeechResponse`, `SpeechDetailResponse` DTO 작성
- [x] MCP 서버 연동 클라이언트 작성
- [x] Spring Batch 기본 구성
  - `SpeechCollectJobConfig` (MCP → MySQL 저장)
- [x] 배치 수동 실행 API (`POST /api/batch/collect`)
- [x] `GlobalExceptionHandler` 기본 에러 처리
- [x] CORS 설정 (`WebConfig`)

#### 완료 기준
- [x] `docker-compose up` → MySQL 정상 기동
- [x] 배치 수동 실행 → MCP에서 연설문 50개 이상 DB 저장
- [x] `GET /api/speeches` → JSON 응답 확인
- [x] Flyway 마이그레이션 실행 확인

---

### Week 2: Elasticsearch & 전문 검색

> 목표: 한국어 형태소 분석 기반 키워드 검색 기능 완성

- [x] `docker-compose.yml`에 Elasticsearch 8.x 추가
  - Nori 플러그인 설치 스크립트/Dockerfile
- [x] `ElasticsearchConfig` 작성
- [x] `SpeechDocument` 작성 (ES 인덱스 매핑)
  - nori_tokenizer 기반 korean 분석기 설정
- [x] `SpeechSearchRepository` 작성 (Spring Data Elasticsearch)
- [x] 배치에 ES 동기화 스텝 추가 (MySQL → Elasticsearch)
- [x] `SearchController` 작성
  - `GET /api/speeches/search?query=&category=&dateFrom=&dateTo=&page=&size=`
- [x] 검색 결과 하이라이팅 처리
- [x] Pagination 응답 형식 통일 (Page 객체)

#### 완료 기준
- [x] "경제 성장" 검색 → Nori 형태소 분석으로 관련 연설문 검색
- [x] 날짜 필터 적용하여 검색 결과 확인
- [x] 페이지네이션 정상 동작

---

### Week 3: 벡터 DB 구축 & 임베딩 파이프라인

> 목표: RAG를 위한 벡터 검색 인프라 완성

- [x] `docker-compose.yml`에 Qdrant 추가
- [x] `QdrantConfig` 작성 (Qdrant Java Client 설정)
- [x] `QdrantClient` 래퍼 작성
  - 컬렉션 생성 (speech_vectors, 1536차원, Cosine)
  - 벡터 upsert, 검색 메서드
- [x] `OpenAiConfig` 작성 (API 키 환경변수 관리)
- [x] `OpenAiClient` 작성
  - `embed(String text)` → float[] (text-embedding-3-small)
- [x] 연설문 청킹 로직 구현
  - 500자 단위, 100자 오버랩
  - `SpeechChunk` Entity 저장
- [x] `EmbeddingJobConfig` 배치 Job 작성
  - Step 1: 연설문 → 청크 분할 → speech_chunks 저장
  - Step 2: 청크 → OpenAI 임베딩 → Qdrant 저장
- [x] 벡터 검색 서비스 구현
  - 쿼리 텍스트 → 임베딩 → Qdrant Top-K 검색
- [x] 배치 수동 실행 API (`POST /api/batch/embed`)

#### 완료 기준
- [x] 배치 실행 → 연설문 100개 벡터화 → Qdrant 저장
- [x] Postman으로 벡터 검색 테스트 → 유사 청크 5개 반환
- [x] Qdrant Dashboard에서 저장된 벡터 확인

---

### Week 4: RAG 파이프라인 & 챗봇 API

> 목표: 질문 → 벡터 검색 → GPT 답변 생성 → 출처 제공

- [x] Flyway `V4__create_chat_tables.sql` 작성
  - `chat_sessions`, `chat_messages`, `message_sources` 테이블
- [x] `ChatSession`, `ChatMessage`, `MessageSource` Entity 작성
- [x] `ChatSessionRepository`, `ChatMessageRepository` 작성
- [x] `RagService` 구현 (핵심 로직)
  1. 질문 텍스트 → OpenAI 임베딩
  2. Qdrant 벡터 검색 (Top 5 유사 청크)
  3. 프롬프트 구성 (System + Context + Question)
  4. OpenAI GPT-4o-mini 호출
  5. 답변 + 출처(speech_id, chunk_id, relevance_score) 반환
- [x] 프롬프트 템플릿 작성
  - System 프롬프트: "연설문 전문가" 역할 부여
  - 출처 명시 지침, 한국어 답변 지침
- [x] `ChatService` 구현 (세션 관리, 메시지 저장)
- [x] `ChatController` 작성
  - `POST /api/chat` (ChatRequest → ChatResponse)
- [x] `ChatRequest`, `ChatResponse` DTO 작성

#### 완료 기준
- [x] "윤석열 대통령의 반도체 정책은?" → GPT가 연설문 기반 답변 생성
- [x] 답변 하단에 참고 연설문 3개 카드 표시
- [x] 출처 카드 클릭 → 연설문 상세 페이지 이동

---

### Week 5: 챗봇 고도화 (스트리밍 & 멀티턴)

> 목표: SSE 실시간 스트리밍 답변 + 대화 컨텍스트 유지 + 캐싱

- [x] `docker-compose.yml`에 Redis 추가
- [x] `RedisConfig` 작성
- [x] SSE 스트리밍 API 구현
  - `POST /api/chat/stream` → `SseEmitter` 반환
  - 이벤트 타입: `token` (텍스트 조각), `sources` (출처), `done` (완료)
- [x] OpenAI 스트리밍 호출 구현 (stream: true)
- [x] 멀티턴 대화 구현
  - 최근 3턴 대화 이력을 프롬프트에 포함
  - `GET /api/chat/sessions/{sessionId}/messages`
  - `DELETE /api/chat/sessions/{sessionId}`
- [x] Redis 캐싱 적용
  - 동일 질문 임베딩 결과 캐싱 (24시간 TTL)
  - 자주 묻는 질문 답변 캐싱

#### 완료 기준
- [x] 질문 입력 → 답변이 한 글자씩 스트리밍 출력
- [x] "경제 정책은?" → "더 자세히 알려줘" (컨텍스트 기억)
- [x] 동일 질문 2회 입력 → 두 번째는 캐시로 빠른 응답

---

### Week 6: 키워드 분석 & 통계 API

> 목표: 키워드 추출 배치 + 통계 API 제공

- [x] Flyway `V3__create_keywords_table.sql` 작성
  - `keywords` 테이블 (word, frequency, month, category)
- [x] `Keyword` Entity + `KeywordRepository` 작성
- [x] 형태소 분석 기반 키워드 추출 로직 구현
  - 불용어 사전 정의
  - 명사 추출 (Nori 활용)
- [x] `KeywordExtractJobConfig` 배치 Job 작성
  - 연설문 → 형태소 분석 → 키워드 추출 → keywords 테이블 집계
- [x] `KeywordService` 구현
- [x] `StatsController` 작성
  - `GET /api/stats/keywords/top?limit=20`
  - `GET /api/stats/keywords/trend?keyword=AI&from=2024-01&to=2024-12`
  - `GET /api/stats/speeches/monthly`
  - `GET /api/stats/summary`
- [x] 배치 수동 실행 API (`POST /api/batch/keywords`)

#### 완료 기준
- [x] 배치 실행 → 키워드 추출 및 집계 완료
- [x] `GET /api/stats/keywords/top` → TOP 20 키워드 반환
- [x] `GET /api/stats/keywords/trend` → 월별 트렌드 데이터 반환

---

### Week 7: 카테고리 분류 & 필터 강화

> 목표: 연설문 자동 분류 + 챗봇/검색에 카테고리 필터 적용

- [ ] `Category` Enum 정의 (ECONOMY, FOREIGN, WELFARE, DEFENSE, ENVIRONMENT, ETC)
- [ ] Flyway `V2__add_category_column.sql` 작성
  - `speeches` 테이블에 `category` VARCHAR(50) 컬럼 추가
- [ ] 키워드 기반 분류 규칙 정의
  - ECONOMY: GDP, 수출, 일자리, 투자, 성장, 경제, 반도체...
  - FOREIGN: 동맹, 협력, 정상회담, 외교, 국제...
  - WELFARE: 민생, 복지, 의료, 교육, 출산...
  - DEFENSE: 안보, 국방, 군사, 미사일...
  - ENVIRONMENT: 기후, 탄소, 에너지, 환경...
- [ ] `CategoryClassifyJobConfig` 배치 Job 작성
- [ ] 기존 데이터 일괄 분류 실행
- [ ] Elasticsearch 인덱스에 category 필드 추가
- [ ] 검색 API에 카테고리 필터 적용
- [ ] RAG 서비스에 카테고리 필터 적용 (Qdrant metadata 필터)
- [ ] `GET /api/stats/speeches/category` 구현
- [ ] 배치 수동 실행 API (`POST /api/batch/classify`)

#### 완료 기준
- [x] 배치 실행 → 전체 연설문 카테고리 자동 분류
- [x] 챗봇에서 "경제" 선택 → 경제 관련 연설문만 참고하여 답변
- [x] 파이 차트용 카테고리 분포 API 정상 응답

---

### Week 8: 성능 최적화 & 배포 준비

> 목표: 전체 서비스 안정화 + Docker 배포 + 성능 튜닝

- [ ] Flyway `V5__add_indexes.sql` 작성 (성능 최적화 인덱스)
- [ ] Redis 캐싱 전략 최종 정리
  - 검색 결과: 5분 TTL
  - 통계 데이터: 1시간 TTL
  - 임베딩: 24시간 TTL
- [ ] Elasticsearch 쿼리 최적화
- [ ] API 응답 시간 모니터링 (Spring Actuator)
- [ ] Health check 엔드포인트 확인
- [ ] 에러 로깅 정리 (Logback)
- [ ] `Dockerfile` 작성 (멀티스테이지 빌드)
- [ ] `docker-compose.yml` 최종 구성 (전체 스택)
- [ ] `.env.example` 작성 (환경변수 템플릿)
- [ ] Flyway 마이그레이션 전체 검증 (처음부터 실행)
- [ ] Spring Batch 스케줄러 설정 (매일 새벽 2시)

#### 완료 기준
- [x] `docker-compose up` 한 번에 전체 앱 실행
- [x] 챗봇 응답 시간 평균 3초 이내
- [x] 검색 응답 시간 500ms 이내
- [x] Flyway 마이그레이션 처음부터 무결하게 실행

---

## 8. 참고 사항

### 개발 환경 사전 준비
- JDK 17 설치 (확인 완료)
- Docker Desktop 설치
- OpenAI API 키 발급 ($5 크레딧)
- IDE: IntelliJ IDEA

### 데이터 소스
- MCP 서버: [data-go-mcp-servers](https://github.com/Koomook/data-go-mcp-servers)
- 대통령 연설문 API 연동
