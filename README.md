# 귀띔 (Guitteum) Backend

AI 기반 대통령 연설문 분석 플랫폼 — Backend

## 기술 스택

| 항목 | 기술 |
|------|------|
| Framework | Spring Boot 3.2.5 |
| Language | Java 17 |
| Build | Gradle 8.x |
| RDBMS | MySQL 8.0 |
| Cache | Redis 7.x |
| 전문 검색 | Elasticsearch 8.x + Nori |
| 벡터 DB | Qdrant 1.7.x |
| DB 마이그레이션 | Flyway 10.x |
| 배치 | Spring Batch |
| 외부 API | OpenAI (text-embedding-3-small, GPT-4o-mini) |
| 데이터 수집 | MCP (Model Context Protocol) — [guitteum-mcp](../guitteum-mcp) (커스텀 Python MCP 서버) |

## 주요 기능

- **RAG 정책 챗봇** — 연설문 기반 AI 질의응답 + SSE 스트리밍 + 멀티턴 대화
- **연설문 전문 검색** — Elasticsearch Nori 형태소 분석, 하이라이팅, 날짜/카테고리 필터
- **벡터 검색** — OpenAI 임베딩 + Qdrant 유사도 검색 (500자 청킹, 100자 오버랩)
- **키워드 대시보드** — Nori 기반 키워드 추출, TOP N, 월별 트렌드
- **카테고리 자동 분류** — 규칙 기반 연설문 분류 (경제/외교/복지/안보/환경), 검색 필터 연동
- **데이터 수집 파이프라인** — MCP 서버 연동 대통령 연설문 자동 수집

## 사전 준비

- JDK 17
- Docker Desktop
- MySQL 8.0 (로컬 또는 Docker)
- Redis 7.x (로컬)
- Python 3.10+ & [uv](https://github.com/astral-sh/uv) (MCP 서버 실행용)
- OpenAI API 키
- data.go.kr API 키

## 빌드 & 실행

```bash
# 인프라 서비스 기동 (Elasticsearch, Qdrant)
docker-compose up -d

# 빌드
./gradlew build

# 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# 테스트
./gradlew test
```

## 데이터 초기화 (배치 실행 순서)

백엔드 기동 후 아래 순서로 배치를 실행하면 전체 데이터가 구성됩니다.

```bash
# 1. 연설문 수집 (MCP 서버 → DB)
curl -X POST http://localhost:8080/api/admin/batch/collect

# 2. ES 인덱싱
curl -X POST http://localhost:8080/api/admin/index/speeches

# 3. 키워드 추출
curl -X POST http://localhost:8080/api/admin/batch/keywords

# 4. 카테고리 분류
curl -X POST http://localhost:8080/api/admin/batch/classify

# 5. 벡터 임베딩 (OpenAI API 호출, 시간 소요)
curl -X POST http://localhost:8080/api/admin/batch/embed
```

> 1번만 실행해도 연설문 목록/상세 조회가 가능합니다. 2~5번은 검색/통계/챗봇 기능 활성화용입니다.

## 환경변수

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `MYSQL_USER` | MySQL 사용자 | `root` |
| `MYSQL_PASSWORD` | MySQL 비밀번호 | `root` |
| `OPENAI_API_KEY` | OpenAI API 키 | - |
| `DATA_GO_API_KEY` | data.go.kr API 키 | - |
| `ELASTICSEARCH_URI` | ES 접속 URI | `http://localhost:9200` |
| `REDIS_HOST` | Redis 호스트 | `localhost` |
| `REDIS_PORT` | Redis 포트 | `6379` |
| `QDRANT_HOST` | Qdrant 호스트 | `localhost` |
| `QDRANT_PORT` | Qdrant 포트 | `6334` |

## API 명세

### 연설문

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/speeches` | 연설문 목록 (페이지네이션) |
| GET | `/api/speeches/{id}` | 연설문 상세 조회 |

### 검색

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/speeches/search` | 키워드 검색 (query, category, dateFrom, dateTo, page, size) |

### 챗봇

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/chat` | 질문 → AI 답변 (JSON) |
| POST | `/api/chat/stream` | 질문 → SSE 스트리밍 답변 |
| GET | `/api/chat/sessions/{sessionId}/messages` | 대화 이력 조회 |
| DELETE | `/api/chat/sessions/{sessionId}` | 대화 세션 삭제 |

### 통계

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/stats/keywords/top` | TOP N 키워드 (limit) |
| GET | `/api/stats/keywords/trend` | 키워드 월별 트렌드 (keyword, from, to) |
| GET | `/api/stats/speeches/monthly` | 월별 연설문 개수 |
| GET | `/api/stats/speeches/category` | 카테고리별 분포 |
| GET | `/api/stats/summary` | 요약 통계 |

### 관리자 (배치 실행)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/admin/batch/collect` | 연설문 수집 (MCP) |
| POST | `/api/admin/batch/embed` | 임베딩 생성 (OpenAI → Qdrant) |
| POST | `/api/admin/batch/keywords` | 키워드 추출 (Nori) |
| POST | `/api/admin/batch/classify` | 카테고리 자동 분류 |
| POST | `/api/admin/index/speeches` | ES 인덱스 재구축 |

## 패키지 구조

```
src/main/java/com/guitteum/
├── api/            # Controller (HTTP 엔드포인트)
├── domain/         # Entity, Repository, Service, DTO
│   ├── chat/       # 챗봇 (세션, 메시지, RAG)
│   ├── speech/     # 연설문
│   └── keyword/    # 키워드 통계
├── infra/          # 외부 시스템 연동
│   ├── openai/     # OpenAI 임베딩 & 챗
│   ├── qdrant/     # 벡터 DB
│   ├── elasticsearch/ # 전문 검색 & Nori 분석
│   ├── mcp/        # MCP 연설문 수집
│   └── redis/      # 캐싱
├── batch/          # Spring Batch Job
└── global/         # 공통 설정, 예외 처리
```

## DB 마이그레이션

| 파일 | 설명 |
|------|------|
| `V1__init_schema.sql` | speeches, speech_chunks 테이블 |
| `V2__add_category_column.sql` | speeches 테이블 category 컬럼 추가 |
| `V3__create_keywords_table.sql` | keywords 테이블 |
| `V4__create_chat_tables.sql` | chat_sessions, chat_messages, message_sources 테이블 |
