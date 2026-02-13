# 귀띔 MCP 서버 (guitteum-mcp) 개발 작업 계획서

> 대통령 연설문 수집용 커스텀 MCP 서버
> 별도 저장소: `guitteum-mcp/`
> 작성일: 2026-02-13

---

## 1. 프로젝트 개요

data.go.kr 공공데이터 API (문화체육관광부_정책브리핑_연설문)에서 대통령 연설문을 수집하여
MCP (Model Context Protocol) 프로토콜로 제공하는 Python 서버.

Spring Boot 백엔드(`guitteum-backend`)의 `SpeechMcpClient`가 이 서버를 subprocess로 실행하여
stdio 트랜스포트를 통해 통신한다.

**기존 외부 패키지**: `data-go-mcp.presidential-speeches@latest` (uvx)
**대체 목표**: `guitteum-mcp` (자체 구축)

---

## 2. 기술 스택

| 항목 | 기술 |
|------|------|
| Language | Python 3.10+ |
| MCP SDK | mcp[cli] 1.x (FastMCP) |
| HTTP Client | httpx |
| XML Parser | xmltodict |
| Build System | hatchling (pyproject.toml) |
| Transport | stdio (JSON-RPC over stdin/stdout) |
| 배포 | uvx / pip |

---

## 3. data.go.kr API 명세

### 기본 정보

| 항목 | 값 |
|------|------|
| API명 | 문화체육관광부_정책브리핑_연설문_API |
| Base URL | `https://apis.data.go.kr/1371000/speechService` |
| Endpoint | `/speechList` |
| Method | GET |
| 응답 형식 | XML |

### 요청 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `serviceKey` | string | O | data.go.kr 인증키 (URL 디코딩) |
| `startDate` | string | O | 조회 시작일 (YYYYMMDD) |
| `endDate` | string | O | 조회 종료일 (YYYYMMDD) |

### 제약 사항

- **조회 범위 최대 3일**. 초과 시 에러 코드 98 반환
- 일일 API 호출 제한: 개발 계정 1,000건

### 응답 필드 (NewsItem)

| API 필드 | 설명 | SpeechData 매핑 |
|----------|------|-----------------|
| `NewsItemId` | 기사 ID | `id` |
| `Title` | 연설 제목 | `title` |
| `DataContents` | 연설 본문 (HTML) | `content` (HTML 태그 제거) |
| `ApproveDate` | 발행일 | `date`, `speech_date` (yyyy-MM-dd), `speech_year` (연도) |
| `SubTitle1` | 부제 (행사/장소 정보) | `location` |
| `OriginalUrl` | 원문 URL | `source_url` |
| `MinisterCode` | 부처/발화자 코드 | `president` (매핑 테이블) |

### 에러 코드

| 코드 | 설명 |
|------|------|
| 200 | 정상 |
| -1 | 인증키 오류 |
| 11 | 필수값 누락 |
| 97 | 날짜 형식 오류 |
| 98 | 조회 범위 3일 초과 |

---

## 4. MCP 도구 정의

백엔드 `SpeechMcpClient.java`가 호출하는 도구와 정확히 일치해야 함.

### `list_speeches`

```
파라미터: page (int, 기본 1), per_page (int, 기본 100)
반환: JSON 배열 (SpeechData[])
```

전체 연설문을 페이지 단위로 반환. 빈 배열 반환 시 수집 종료.

### `search_speeches`

```
파라미터: president (str), page (int, 기본 1), per_page (int, 기본 100)
반환: JSON 배열 (SpeechData[])
```

대통령명으로 필터링된 연설문 반환.

### 응답 스키마 (SpeechData)

```json
{
  "id": "12345",
  "president": "윤석열",
  "title": "제78주년 광복절 경축사",
  "content": "존경하는 국민 여러분...",
  "date": "2023-08-15",
  "speech_date": "2023-08-15",
  "speech_year": 2023,
  "location": "서울",
  "source_url": "https://www.korea.kr/..."
}
```

백엔드 `parseResult()`가 JSON 배열(`[...]`)과 `{"data": [...]}` 래퍼 모두 처리 가능.
단순 JSON 배열 반환 권장.

---

## 5. 프로젝트 구조

```
guitteum-mcp/
├── pyproject.toml              # 패키지 메타데이터, 의존성, entry point
├── README.md
├── .gitignore
├── .env.example                # API_KEY=your_data_go_kr_key
│
├── src/
│   └── guitteum_mcp/
│       ├── __init__.py         # 패키지 버전
│       ├── __main__.py         # python -m guitteum_mcp 진입점
│       ├── server.py           # FastMCP 서버 + 도구 등록
│       ├── api_client.py       # httpx 기반 data.go.kr API 호출
│       ├── models.py           # Pydantic SpeechData 모델
│       ├── mapper.py           # XML → SpeechData 변환 (HTML 스트립)
│       ├── pagination.py       # 3일 윈도우 순회 + 가상 페이지네이션
│       └── config.py           # API_KEY 환경변수 로딩
│
└── tests/
    ├── __init__.py
    ├── test_api_client.py
    ├── test_mapper.py
    ├── test_pagination.py
    └── fixtures/
        └── sample_response.xml
```

---

## 6. 핵심 설계

### 3일 윈도우 제약 해결

data.go.kr API가 3일 초과 조회를 거부하므로, 전체 수집 시 날짜 윈도우를 반복 순회해야 함.

**채택 방식: 인메모리 캐시**
1. 첫 `list_speeches` 호출 시 2008-01-01 ~ 오늘까지 3일 단위로 API 반복 호출
2. 수집한 전체 연설문을 메모리에 캐시
3. `page`/`per_page` 파라미터로 캐시에서 슬라이싱하여 반환
4. MCP 서버 프로세스 종료 시 캐시 소멸 (배치 1회 = 프로세스 1회)

**이유**: 백엔드 `collectAll()`이 MCP 서버를 1회 실행 → 전체 페이지 순회 → 종료하는 패턴.
프로세스 수명 동안만 캐시하면 충분.

### stdout 오염 방지

MCP stdio 트랜스포트는 stdout을 JSON-RPC 통신에 사용.
**모든 로깅은 stderr로** (`logging.basicConfig(stream=sys.stderr)`).

### 환경변수

백엔드 `SpeechMcpClient.java`가 `ServerParameters.addEnvVar("API_KEY", apiKey)`로 전달.
MCP 서버는 `os.environ.get("API_KEY")`로 수신.

### 대통령명 매핑

`MinisterCode` 또는 제목 패턴에서 대통령명 추출. 매핑 딕셔너리:
```python
PRESIDENT_MAP = {
    "대통령": "현직",  # 기간별 자동 매핑
    # 기간별 매핑
    # 2022-05~ : 윤석열
    # 2017-05~2022-05 : 문재인
    # 2013-02~2017-03 : 박근혜
    # 2008-02~2013-02 : 이명박
}
```

---

## 7. 구현 단계

### Phase 1: 프로젝트 스캐폴딩

- [x] `guitteum-mcp/` 디렉토리 생성 (별도 저장소)
- [x] `pyproject.toml` 작성 (hatchling, 의존성, entry point)
- [x] `src/guitteum_mcp/` 패키지 구조 생성
- [x] `.gitignore`, `.env.example` 작성

### Phase 2: Config + Models

- [x] `config.py` 구현 (API_KEY 환경변수 로딩)
- [x] `models.py` 구현 (Pydantic SpeechData — 백엔드 스키마와 일치)

### Phase 3: API Client + Mapper + Pagination

- [x] `api_client.py` 구현 (httpx AsyncClient, data.go.kr API 호출)
- [x] `mapper.py` 구현 (XML → SpeechData 변환, HTML 태그 제거, 날짜 파싱)
- [x] `pagination.py` 구현 (3일 윈도우 생성기, 가상 페이지네이션)

### Phase 4: FastMCP 서버 + 도구 구현

- [x] `server.py` 구현 (FastMCP 인스턴스, 도구 등록)
- [x] `list_speeches` 도구 구현
- [x] `search_speeches` 도구 구현
- [x] `__main__.py` 진입점 작성

### Phase 5: 통합 테스트

- [ ] 단독 실행 테스트 (`uvx guitteum-mcp`)
- [ ] 백엔드 `application.yml`의 `mcp-args`를 `guitteum-mcp`로 변경
- [ ] `POST /api/admin/batch/collect` → DB 저장 확인

---

## 8. 백엔드 설정 변경

`application.yml`:
```yaml
data-go:
  api-key: ${DATA_GO_API_KEY:}
  mcp-command: uvx
  mcp-args: guitteum-mcp          # 변경: data-go-mcp.presidential-speeches@latest → guitteum-mcp
```

로컬 개발 시:
```yaml
data-go:
  mcp-command: uv
  mcp-args: --directory ../guitteum-mcp run guitteum-mcp
```

---

## 9. 검증

- [ ] `uvx guitteum-mcp` 또는 `python -m guitteum_mcp` → 정상 실행
- [ ] MCP 핸드셰이크 (initialize) 성공
- [ ] `list_speeches(page=1, per_page=10)` → 연설문 JSON 배열 반환
- [ ] `search_speeches(president="윤석열", page=1, per_page=5)` → 필터 결과 반환
- [ ] 백엔드 `POST /api/admin/batch/collect` → 50건 이상 DB 저장
- [ ] stdout 오염 없음 (로깅 전부 stderr)
- [ ] API_KEY 미설정 시 명확한 에러 메시지

---

## 10. 참고

### 백엔드 연동 파일
- `infra/mcp/SpeechMcpClient.java` — MCP 클라이언트 (도구명, 파라미터, API_KEY 전달, 30초 타임아웃)
- `infra/mcp/SpeechData.java` — 응답 레코드 스키마
- `batch/job/SpeechCollectJobConfig.java` — 수집 배치 (날짜 파싱, 중복 검사, 필드 매핑)
- `application.yml` — `data-go.mcp-command`, `data-go.mcp-args` 설정
