# 귀띔 (Guitteum) 개발 작업 계획서

> AI 기반 대통령 연설문 분석 플랫폼
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

### Backend
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

### Frontend
| 항목 | 기술 |
|------|------|
| Framework | Vue 3.4.x (Composition API) |
| Build Tool | Vite 5.x |
| UI 라이브러리 | shadcn-vue 0.10.x |
| CSS | Tailwind CSS 3.4.x |
| 상태 관리 | Pinia 2.x |
| 차트 | Apache ECharts 5.x |
| HTTP | Axios 1.x |
| 라우팅 | Vue Router 4.x |
| 아이콘 | lucide-vue-next |

### Infrastructure
| 항목 | 기술 |
|------|------|
| 컨테이너 | Docker & Docker Compose |
| VCS | Git & GitHub |

---

## 3. 백엔드 패키지 구조

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

## 4. 프론트엔드 디렉토리 구조

```
guitteum-frontend/
├── index.html
├── package.json
├── vite.config.js
├── tailwind.config.js
├── postcss.config.js
│
├── public/
│   └── favicon.ico
│
└── src/
    ├── main.js
    ├── App.vue
    │
    ├── api/                       # API 호출 모듈
    │   ├── axios.js               # Axios 인스턴스
    │   ├── speechApi.js
    │   ├── chatApi.js
    │   ├── searchApi.js
    │   └── statsApi.js
    │
    ├── router/
    │   └── index.js
    │
    ├── stores/                    # Pinia 스토어
    │   ├── chatStore.js
    │   ├── speechStore.js
    │   └── statsStore.js
    │
    ├── views/                     # 페이지 컴포넌트
    │   ├── HomeView.vue           # 대시보드 (메인)
    │   ├── ChatView.vue           # 챗봇 페이지
    │   ├── SearchView.vue         # 검색 페이지
    │   ├── SpeechDetailView.vue   # 연설문 상세
    │   └── StatsView.vue          # 통계 페이지
    │
    ├── components/                # 재사용 컴포넌트
    │   ├── layout/
    │   │   ├── AppHeader.vue
    │   │   ├── AppSidebar.vue
    │   │   └── MobileMenu.vue
    │   ├── chat/
    │   │   ├── ChatBubble.vue
    │   │   ├── ChatInput.vue
    │   │   ├── SourceCard.vue
    │   │   └── CategoryFilter.vue
    │   ├── search/
    │   │   ├── SearchBar.vue
    │   │   ├── SearchResult.vue
    │   │   └── SearchFilter.vue
    │   ├── dashboard/
    │   │   ├── WordCloud.vue
    │   │   ├── LineChart.vue
    │   │   ├── PieChart.vue
    │   │   └── StatCard.vue
    │   └── ui/                    # shadcn-vue 컴포넌트
    │       ├── button/
    │       ├── card/
    │       ├── input/
    │       ├── badge/
    │       ├── dialog/
    │       ├── collapsible/
    │       ├── skeleton/
    │       ├── toast/
    │       └── ...
    │
    ├── composables/               # 재사용 로직
    │   ├── useSSE.js              # SSE 스트리밍
    │   └── useDarkMode.js         # 다크모드 토글
    │
    ├── assets/
    │   └── css/
    │       └── main.css           # Tailwind + 커스텀 CSS
    │
    └── lib/
        └── utils.js               # cn() 유틸리티 등
```

---

## 5. Docker Compose 구성

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

## 6. Flyway 마이그레이션 계획

| 파일명 | 적용 주차 | 설명 |
|--------|----------|------|
| `V1__init_schema.sql` | Week 1 | speeches, speech_chunks 테이블 생성 |
| `V2__add_category_column.sql` | Week 7 | speeches에 category 컬럼 추가 |
| `V3__create_keywords_table.sql` | Week 6 | keywords 테이블 생성 |
| `V4__create_chat_tables.sql` | Week 4 | chat_sessions, chat_messages, message_sources 생성 |
| `V5__add_indexes.sql` | Week 8 | 성능 최적화 인덱스 추가 |

---

## 7. API 명세 목록

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

## 8. 주차별 상세 작업 계획

---

### Week 1: 프로젝트 셋업 & 기본 데이터 수집

> 목표: 프로젝트 초기화 → MCP 연동 → DB 저장 → 연설문 목록 화면 출력

#### Backend 작업

- [ ] Spring Boot 프로젝트 생성 (Spring Initializr)
  - Spring Web, Spring Data JPA, MySQL Driver, Flyway, Validation, Actuator
  - `build.gradle` 의존성 정의
  - `settings.gradle` 작성
- [ ] `.gitignore` 작성 (Java/Gradle/IntelliJ 대상)
- [ ] `docker-compose.yml` 작성 (MySQL 8.0 단독 실행)
- [ ] `application.yml` / `application-local.yml` 설정
  - MySQL 접속 정보
  - Flyway 설정
  - 서버 포트, 로깅 레벨
- [ ] Flyway `V1__init_schema.sql` 작성
  - `speeches` 테이블 (id, title, content, speech_date, event_name, created_at, updated_at)
  - `speech_chunks` 테이블 (id, speech_id, chunk_index, content, vector_id)
- [ ] `Speech` Entity + `SpeechRepository` 작성
- [ ] `SpeechService` 작성 (목록 조회, 상세 조회)
- [ ] `SpeechController` 작성
  - `GET /api/speeches` (Pageable)
  - `GET /api/speeches/{id}`
- [ ] `SpeechResponse`, `SpeechDetailResponse` DTO 작성
- [ ] MCP 서버 연동 클라이언트 작성
- [ ] Spring Batch 기본 구성
  - `SpeechCollectJobConfig` (MCP → MySQL 저장)
- [ ] 배치 수동 실행 API (`POST /api/batch/collect`)
- [ ] `GlobalExceptionHandler` 기본 에러 처리
- [ ] CORS 설정 (`WebConfig`)

#### Frontend 작업

- [ ] Vue 3 + Vite 프로젝트 생성 (`guitteum-frontend/`)
- [ ] Tailwind CSS 설치 및 설정
- [ ] shadcn-vue 설치 및 초기 설정
  - 기본 컴포넌트: Button, Card, Input, Badge, Skeleton, Table
- [ ] Vue Router 설정 (라우트 정의)
- [ ] Axios 인스턴스 생성 (`api/axios.js`)
- [ ] `speechApi.js` 작성 (목록, 상세 API 호출)
- [ ] `AppHeader.vue` 구현 (반응형 네비게이션)
- [ ] `MobileMenu.vue` 구현 (햄버거 메뉴)
- [ ] `HomeView.vue` 초안 (간단한 히어로 섹션 + 연설문 목록 테이블)
- [ ] 기본 CSS 변수 설정 (Linear 스타일 색상 팔레트, 다크모드)
- [ ] 폰트 설정 (Inter + Pretendard)

#### 완료 기준
- [x] `docker-compose up` → MySQL 정상 기동
- [x] 배치 수동 실행 → MCP에서 연설문 50개 이상 DB 저장
- [x] `GET /api/speeches` → JSON 응답 확인
- [x] 프론트엔드 화면에 연설문 목록 테이블 표시
- [x] 모바일/태블릿/데스크톱 레이아웃 정상 작동
- [x] Flyway 마이그레이션 실행 확인

---

### Week 2: Elasticsearch & 전문 검색

> 목표: 한국어 형태소 분석 기반 키워드 검색 기능 완성

#### Backend 작업

- [ ] `docker-compose.yml`에 Elasticsearch 8.x 추가
  - Nori 플러그인 설치 스크립트/Dockerfile
- [ ] `ElasticsearchConfig` 작성
- [ ] `SpeechDocument` 작성 (ES 인덱스 매핑)
  - nori_tokenizer 기반 korean 분석기 설정
- [ ] `SpeechSearchRepository` 작성 (Spring Data Elasticsearch)
- [ ] 배치에 ES 동기화 스텝 추가 (MySQL → Elasticsearch)
- [ ] `SearchController` 작성
  - `GET /api/speeches/search?query=&category=&dateFrom=&dateTo=&page=&size=`
- [ ] 검색 결과 하이라이팅 처리
- [ ] Pagination 응답 형식 통일 (Page 객체)

#### Frontend 작업

- [ ] `SearchView.vue` 구현
  - 반응형 검색창 (모바일: 전체 너비, 데스크톱: 중앙 정렬)
  - 검색 결과 카드 리스트
- [ ] `SearchBar.vue` 컴포넌트
- [ ] `SearchFilter.vue` 컴포넌트
  - 날짜 범위 선택 (모바일: 드롭다운, 데스크톱: 인라인)
  - 카테고리 필터 (칩 형태)
- [ ] `SearchResult.vue` 컴포넌트 (검색 결과 개별 카드)
- [ ] Pagination 컴포넌트 (shadcn-vue)
- [ ] `searchApi.js` 작성
- [ ] `SpeechDetailView.vue` 구현 (연설문 전문 보기)
- [ ] 라우터에 검색, 상세 페이지 추가

#### 완료 기준
- [x] "경제 성장" 검색 → Nori 형태소 분석으로 관련 연설문 검색
- [x] 날짜 필터 적용하여 검색 결과 확인
- [x] 페이지네이션 정상 동작
- [x] 모바일에서 필터 사용 가능

---

### Week 3: 벡터 DB 구축 & 임베딩 파이프라인

> 목표: RAG를 위한 벡터 검색 인프라 완성

#### Backend 작업

- [ ] `docker-compose.yml`에 Qdrant 추가
- [ ] `QdrantConfig` 작성 (Qdrant Java Client 설정)
- [ ] `QdrantClient` 래퍼 작성
  - 컬렉션 생성 (speech_vectors, 1536차원, Cosine)
  - 벡터 upsert, 검색 메서드
- [ ] `OpenAiConfig` 작성 (API 키 환경변수 관리)
- [ ] `OpenAiClient` 작성
  - `embed(String text)` → float[] (text-embedding-3-small)
- [ ] 연설문 청킹 로직 구현
  - 500자 단위, 100자 오버랩
  - `SpeechChunk` Entity 저장
- [ ] `EmbeddingJobConfig` 배치 Job 작성
  - Step 1: 연설문 → 청크 분할 → speech_chunks 저장
  - Step 2: 청크 → OpenAI 임베딩 → Qdrant 저장
- [ ] 벡터 검색 서비스 구현
  - 쿼리 텍스트 → 임베딩 → Qdrant Top-K 검색
- [ ] 배치 수동 실행 API (`POST /api/batch/embed`)

#### Frontend 작업

- [ ] Pinia 스토어 기본 구조 작성 (`speechStore.js`)
- [ ] 로딩 상태 관리 (전역 로딩 인디케이터)
- [ ] Skeleton 컴포넌트 적용 (목록, 상세 페이지)
- [ ] Toast 알림 구현 (에러 핸들링)
- [ ] 검색 페이지 UX 개선 (디바운스 등)

#### 완료 기준
- [x] 배치 실행 → 연설문 100개 벡터화 → Qdrant 저장
- [x] Postman으로 벡터 검색 테스트 → 유사 청크 5개 반환
- [x] Qdrant Dashboard에서 저장된 벡터 확인

---

### Week 4: RAG 파이프라인 & 챗봇 API

> 목표: 질문 → 벡터 검색 → GPT 답변 생성 → 출처 제공

#### Backend 작업

- [ ] Flyway `V4__create_chat_tables.sql` 작성
  - `chat_sessions`, `chat_messages`, `message_sources` 테이블
- [ ] `ChatSession`, `ChatMessage`, `MessageSource` Entity 작성
- [ ] `ChatSessionRepository`, `ChatMessageRepository` 작성
- [ ] `RagService` 구현 (핵심 로직)
  1. 질문 텍스트 → OpenAI 임베딩
  2. Qdrant 벡터 검색 (Top 5 유사 청크)
  3. 프롬프트 구성 (System + Context + Question)
  4. OpenAI GPT-4o-mini 호출
  5. 답변 + 출처(speech_id, chunk_id, relevance_score) 반환
- [ ] 프롬프트 템플릿 작성
  - System 프롬프트: "연설문 전문가" 역할 부여
  - 출처 명시 지침, 한국어 답변 지침
- [ ] `ChatService` 구현 (세션 관리, 메시지 저장)
- [ ] `ChatController` 작성
  - `POST /api/chat` (ChatRequest → ChatResponse)
- [ ] `ChatRequest`, `ChatResponse` DTO 작성

#### Frontend 작업

- [ ] `ChatView.vue` 구현
  - 반응형 채팅 레이아웃 (모바일: 전체 화면, 데스크톱: 중앙 정렬)
  - 메시지 목록 영역 (스크롤)
  - 입력창 + 전송 버튼
- [ ] `ChatBubble.vue` 구현
  - 사용자 메시지: 오른쪽 정렬, primary 색상
  - AI 메시지: 왼쪽 정렬, muted 색상
  - 메시지 너비: 모바일 85%, 데스크톱 70%
- [ ] `SourceCard.vue` 구현
  - Collapsible 출처 카드 (shadcn-vue Collapsible)
  - 연설문 제목, 날짜 표시 → 클릭 시 상세 페이지 이동
- [ ] `ChatInput.vue` 구현 (Enter 전송, 버튼 전송)
- [ ] `chatApi.js` 작성
- [ ] `chatStore.js` 구현 (세션 관리, 메시지 목록)
- [ ] 라우터에 챗봇 페이지 추가

#### 완료 기준
- [x] "윤석열 대통령의 반도체 정책은?" → GPT가 연설문 기반 답변 생성
- [x] 답변 하단에 참고 연설문 3개 카드 표시
- [x] 출처 카드 클릭 → 연설문 상세 페이지 이동
- [x] 모바일에서 채팅 UI 정상 작동

---

### Week 5: 챗봇 고도화 (스트리밍 & 멀티턴)

> 목표: SSE 실시간 스트리밍 답변 + 대화 컨텍스트 유지 + 캐싱

#### Backend 작업

- [ ] `docker-compose.yml`에 Redis 추가
- [ ] `RedisConfig` 작성
- [ ] SSE 스트리밍 API 구현
  - `POST /api/chat/stream` → `SseEmitter` 반환
  - 이벤트 타입: `token` (텍스트 조각), `sources` (출처), `done` (완료)
- [ ] OpenAI 스트리밍 호출 구현 (stream: true)
- [ ] 멀티턴 대화 구현
  - 최근 3턴 대화 이력을 프롬프트에 포함
  - `GET /api/chat/sessions/{sessionId}/messages`
  - `DELETE /api/chat/sessions/{sessionId}`
- [ ] Redis 캐싱 적용
  - 동일 질문 임베딩 결과 캐싱 (24시간 TTL)
  - 자주 묻는 질문 답변 캐싱

#### Frontend 작업

- [ ] `useSSE.js` composable 구현
  - EventSource API로 SSE 연결
  - `token` 이벤트 → 메시지에 점진적 추가
  - `sources` 이벤트 → 출처 카드 렌더링
  - `done` 이벤트 → 스트리밍 종료 처리
- [ ] 타이핑 효과 구현 (CSS 애니메이션)
- [ ] "새 대화" 버튼 구현 (세션 초기화)
- [ ] 대화 히스토리 사이드 패널
  - 모바일: Sheet (슬라이드 인)
  - 데스크톱: Sidebar
- [ ] 로딩 인디케이터 (AI 답변 생성 중)
- [ ] 에러 처리 (네트워크 오류, 타임아웃)

#### 완료 기준
- [x] 질문 입력 → 답변이 한 글자씩 스트리밍 출력
- [x] "경제 정책은?" → "더 자세히 알려줘" (컨텍스트 기억)
- [x] 동일 질문 2회 입력 → 두 번째는 캐시로 빠른 응답
- [x] 모바일에서 타이핑 애니메이션 부드러움

---

### Week 6: 키워드 분석 & 대시보드

> 목표: 연설문 통계 시각화 (워드클라우드, 차트)

#### Backend 작업

- [ ] Flyway `V3__create_keywords_table.sql` 작성
  - `keywords` 테이블 (word, frequency, month, category)
- [ ] `Keyword` Entity + `KeywordRepository` 작성
- [ ] 형태소 분석 기반 키워드 추출 로직 구현
  - 불용어 사전 정의
  - 명사 추출 (Nori 활용)
- [ ] `KeywordExtractJobConfig` 배치 Job 작성
  - 연설문 → 형태소 분석 → 키워드 추출 → keywords 테이블 집계
- [ ] `KeywordService` 구현
- [ ] `StatsController` 작성
  - `GET /api/stats/keywords/top?limit=20`
  - `GET /api/stats/keywords/trend?keyword=AI&from=2024-01&to=2024-12`
  - `GET /api/stats/speeches/monthly`
  - `GET /api/stats/summary`
- [ ] 배치 수동 실행 API (`POST /api/batch/keywords`)

#### Frontend 작업

- [ ] Apache ECharts 설치 및 설정
- [ ] `HomeView.vue` 대시보드 리뉴얼 (Linear 스타일)
  - 히어로 섹션 (AI 질문 입력창)
  - 통계 요약 카드 4개 (총 연설문, 주요 키워드, AI 질문 수, 평균 응답)
  - 차트 그리드
- [ ] `StatCard.vue` 구현 (큰 숫자 + 트렌드 표시)
- [ ] `WordCloud.vue` 구현 (ECharts wordCloud)
  - 키워드 클릭 → 관련 연설문 검색 페이지 이동
- [ ] `LineChart.vue` 구현 (월별 연설 추이)
  - Linear 스타일 그라디언트 area
  - 6개월/1년 토글
- [ ] `PieChart.vue` 구현 (카테고리별 분포)
  - 범례 리스트 (Linear 스타일)
- [ ] `statsApi.js` 작성
- [ ] `statsStore.js` 구현
- [ ] 반응형 차트 (모바일: 1열, 태블릿 이상: 2열 그리드)
- [ ] 터치 제스처 지원 (차트 확대/축소)

#### 완료 기준
- [x] 대시보드에 TOP 20 키워드 워드클라우드 표시
- [x] "AI" 클릭 → 월별 트렌드 차트 표시
- [x] 월별 연설 개수 라인 차트 정상 렌더링
- [x] 모바일에서 차트가 화면 너비에 맞게 반응형 렌더링

---

### Week 7: 카테고리 분류 & 필터 강화

> 목표: 연설문 자동 분류 + 챗봇/검색에 카테고리 필터 적용

#### Backend 작업

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

#### Frontend 작업

- [ ] `CategoryFilter.vue` 구현
  - 모바일: 가로 스크롤 칩 (Badge)
  - 데스크톱: 전체 표시
- [ ] 챗봇 페이지에 카테고리 필터 추가
  - 카테고리 선택 → 해당 분야만 검색하여 답변
- [ ] 검색 페이지 카테고리 필터 연동
- [ ] 대시보드 파이 차트 데이터 연동 (실제 API)
- [ ] 연설문 상세 페이지에 카테고리 Badge 표시

#### 완료 기준
- [x] 배치 실행 → 전체 연설문 카테고리 자동 분류
- [x] 챗봇에서 "경제" 선택 → 경제 관련 연설문만 참고하여 답변
- [x] 파이 차트로 카테고리 분포 표시
- [x] 모바일에서 카테고리 칩 가로 스크롤

---

### Week 8: 성능 최적화 & 배포 준비

> 목표: 전체 서비스 안정화 + Docker 배포 + 성능 튜닝

#### Backend 작업

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

#### Frontend 작업

- [ ] 반응형 최종 점검
  - iPhone SE (375px)
  - iPhone 14 (390px)
  - iPad (768px)
  - Desktop (1280px)
- [ ] 터치 타겟 크기 검증 (최소 44x44px)
- [ ] 모든 페이지에 Skeleton 로딩 적용
- [ ] 에러 바운더리 구현
- [ ] `useDarkMode.js` 다크모드 토글 구현
- [ ] PWA 기본 설정 (manifest.json)
- [ ] Vite 빌드 최적화 (코드 스플리팅)
- [ ] `StatsView.vue` 구현 (챗봇 사용 통계)
  - 총 질문 수
  - 평균 응답 시간
  - 인기 질문 TOP 5

#### 완료 기준
- [x] `docker-compose up` 한 번에 전체 앱 실행
- [x] 챗봇 응답 시간 평균 3초 이내
- [x] 검색 응답 시간 500ms 이내
- [x] 모바일 화면에서 모든 기능 정상 작동
- [x] Flyway 마이그레이션 처음부터 무결하게 실행
- [x] Lighthouse 모바일 점수 90+ 목표

---

## 9. 참고 사항

### 개발 환경 사전 준비
- JDK 17 설치 (확인 완료)
- Docker Desktop 설치
- Node.js 18+ 설치
- OpenAI API 키 발급 ($5 크레딧)
- IDE: IntelliJ IDEA (Backend) + VS Code (Frontend)

### 데이터 소스
- MCP 서버: [data-go-mcp-servers](https://github.com/Koomook/data-go-mcp-servers)
- 대통령 연설문 API 연동

### 디자인 레퍼런스
- [Linear.app](https://linear.app/) - 대시보드 및 전체 UI
- 색상: 회색조 기반 + 포인트 컬러 (#5E6AD2)
- 폰트: Inter + Pretendard
