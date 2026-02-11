# 귀띔 (Guitteum) Backend - Claude Code 가이드

AI 기반 대통령 연설문 분석 플랫폼 (Spring Boot).
상세 스펙은 `WORK_BACKEND_PLAN.md`, `guitteum_proposal.md` 참조.

## 기술 스택

Java 17 / Spring Boot 3.2.x / Gradle 8.x / MySQL 8.0 / Flyway 10.x / Redis 7.x / Elasticsearch 8.x + Nori / Qdrant 1.7.x / OpenAI API / Spring Batch

## 빌드 & 실행

```bash
docker-compose up -d
./gradlew build
./gradlew bootRun --args='--spring.profiles.active=local'
./gradlew test
```

## 패키지 구조 (반드시 준수)

```
src/main/java/com/guitteum/
├── api/          # Controller만. 비즈니스 로직 금지. Service 호출 후 DTO 반환
├── domain/       # Entity, Repository, Service, DTO. 핵심 비즈니스 로직
│   ├── chat/     # entity/ repository/ service/ dto/
│   ├── speech/
│   └── keyword/
├── infra/        # 외부 시스템 연동 (OpenAI, Qdrant, ES, Redis). Config + Client
├── batch/        # Spring Batch. job/ reader/ processor/ writer/
└── global/       # config/ exception/ common/
```

## 코딩 컨벤션

### Java 스타일

- DTO는 record 또는 불변 클래스로 작성
- Entity에 `@Builder` 사용 시 `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 함께 선언
- Entity PK: `Long` (AUTO_INCREMENT)
- 날짜: `LocalDateTime` (Java) / `DATETIME` (MySQL)
- 모든 Entity에 `created_at` 포함. 수정 가능 Entity는 `updated_at` 추가

### 네이밍

- 패키지: 소문자 단수 (`speech`, `chat`)
- Entity: 단수형 PascalCase (`Speech`, `ChatMessage`)
- Repository: `{Entity}Repository`
- Service: `{Domain}Service`
- Controller: `{Domain}Controller`
- DTO: `{Domain}Response`, `{Domain}Request`
- Batch Job: `{기능}JobConfig`
- DB 테이블/컬럼: snake_case

### API 응답

- 목록: Spring `Page<T>` + Pageable
- 단건: DTO 직접 반환
- 에러: `ErrorResponse(code, message, timestamp)`
- 경로 prefix: `/api/`

### Flyway

- 경로: `src/main/resources/db/migration/`
- 파일명: `V{번호}__{설명}.sql` (더블 언더스코어)
- 적용된 마이그레이션 파일 수정 금지. 변경은 새 버전 추가
- charset: `utf8mb4` / collate: `utf8mb4_unicode_ci` / Engine: `InnoDB`

### 설정 파일

- `application.yml` (공통) / `application-local.yml` (로컬) / `application-prod.yml` (운영)
- 민감 정보(API 키 등)는 환경변수로 관리. yml에 직접 기입 금지

## 주의사항

- 인증 기능 없음. 세션 ID(UUID) 기반 대화 관리
- 프론트엔드 별도 저장소. CORS 설정 필수
- Flyway 번호 순서와 적용 주차 불일치 (V1→W1, V4→W4, V3→W6, V2→W7, V5→W8)
