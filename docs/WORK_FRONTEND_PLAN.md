# 귀띔 (Guitteum) 프론트엔드 개발 작업 계획서

> AI 기반 대통령 연설문 분석 플랫폼 — Frontend
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
| Framework | Vue 3.4.x (Composition API) |
| Build Tool | Vite 5.x |
| UI 라이브러리 | shadcn-vue 0.10.x |
| CSS | Tailwind CSS 3.4.x |
| 상태 관리 | Pinia 2.x |
| 차트 | Apache ECharts 5.x |
| HTTP | Axios 1.x |
| 라우팅 | Vue Router 4.x |
| 아이콘 | lucide-vue-next |

---

## 3. 디렉토리 구조

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

## 4. 연동 API 목록

### 연설문 API
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/speeches` | 연설문 목록 (페이지네이션) |
| GET | `/api/speeches/{id}` | 연설문 상세 조회 |

### 검색 API
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/speeches/search` | 키워드 검색 (query, category, dateFrom, dateTo, page, size) |

### 챗봇 API
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/chat` | 질문 → AI 답변 (JSON) |
| POST | `/api/chat/stream` | 질문 → SSE 스트리밍 답변 |
| GET | `/api/chat/sessions/{sessionId}/messages` | 대화 이력 조회 |
| DELETE | `/api/chat/sessions/{sessionId}` | 대화 세션 삭제 |

### 통계 API
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/stats/keywords/top` | TOP N 키워드 (limit) |
| GET | `/api/stats/keywords/trend` | 키워드 월별 트렌드 (keyword, from, to) |
| GET | `/api/stats/speeches/monthly` | 월별 연설문 개수 |
| GET | `/api/stats/speeches/category` | 카테고리별 분포 |
| GET | `/api/stats/summary` | 요약 통계 (총 연설문 수, 키워드 수 등) |

---

## 5. 주차별 상세 작업 계획

---

### Week 1: 프로젝트 셋업 & 기본 화면

> 목표: Vue 프로젝트 초기화 → 레이아웃 → 연설문 목록 화면 출력

- [x] Vue 3 + Vite 프로젝트 생성 (`guitteum-frontend/`)
- [x] Tailwind CSS 설치 및 설정
- [x] shadcn-vue 설치 및 초기 설정
  - 기본 컴포넌트: Button, Card, Input, Badge, Skeleton, Table
- [x] Vue Router 설정 (라우트 정의)
- [x] Axios 인스턴스 생성 (`api/axios.js`)
- [x] `speechApi.js` 작성 (목록, 상세 API 호출)
- [x] `AppHeader.vue` 구현 (반응형 네비게이션)
- [x] `MobileMenu.vue` 구현 (햄버거 메뉴)
- [x] `HomeView.vue` 초안 (간단한 히어로 섹션 + 연설문 목록 테이블)
- [x] 기본 CSS 변수 설정 (Linear 스타일 색상 팔레트, 다크모드)
- [x] 폰트 설정 (Inter + Pretendard)

#### 완료 기준
- [x] 프론트엔드 화면에 연설문 목록 테이블 표시
- [x] 모바일/태블릿/데스크톱 레이아웃 정상 작동

---

### Week 2: 검색 페이지

> 목표: 검색 UI 구현 + 연설문 상세 페이지

- [x] `SearchView.vue` 구현
  - 반응형 검색창 (모바일: 전체 너비, 데스크톱: 중앙 정렬)
  - 검색 결과 카드 리스트
- [x] `SearchBar.vue` 컴포넌트
- [x] `SearchFilter.vue` 컴포넌트
  - 날짜 범위 선택 (모바일: 드롭다운, 데스크톱: 인라인)
  - 카테고리 필터 (칩 형태)
- [x] `SearchResult.vue` 컴포넌트 (검색 결과 개별 카드)
- [x] Pagination 컴포넌트 (shadcn-vue)
- [x] `searchApi.js` 작성
- [x] `SpeechDetailView.vue` 구현 (연설문 전문 보기)
- [x] 라우터에 검색, 상세 페이지 추가

#### 완료 기준
- [x] 검색 결과 카드 리스트 정상 렌더링
- [x] 페이지네이션 정상 동작
- [x] 모바일에서 필터 사용 가능

---

### Week 3: UX 개선 & 상태 관리

> 목표: 전역 상태 관리 + 로딩/에러 UX 완성

- [x] Pinia 스토어 기본 구조 작성 (`speechStore.js`)
- [x] 로딩 상태 관리 (전역 로딩 인디케이터)
- [x] Skeleton 컴포넌트 적용 (목록, 상세 페이지)
- [x] Toast 알림 구현 (에러 핸들링)
- [x] 검색 페이지 UX 개선 (디바운스 등)

#### 완료 기준
- [x] Skeleton 로딩 UI 정상 표시
- [x] API 에러 시 Toast 알림 표시
- [x] 검색 디바운스 적용 확인

---

### Week 4: 챗봇 UI

> 목표: 채팅 인터페이스 구현 (JSON 응답 기반)

- [x] `ChatView.vue` 구현
  - 반응형 채팅 레이아웃 (모바일: 전체 화면, 데스크톱: 중앙 정렬)
  - 메시지 목록 영역 (스크롤)
  - 입력창 + 전송 버튼
- [x] `ChatBubble.vue` 구현
  - 사용자 메시지: 오른쪽 정렬, primary 색상
  - AI 메시지: 왼쪽 정렬, muted 색상
  - 메시지 너비: 모바일 85%, 데스크톱 70%
- [x] `SourceCard.vue` 구현
  - Collapsible 출처 카드 (shadcn-vue Collapsible)
  - 연설문 제목, 날짜 표시 → 클릭 시 상세 페이지 이동
- [x] `ChatInput.vue` 구현 (Enter 전송, 버튼 전송)
- [x] `chatApi.js` 작성
- [x] `chatStore.js` 구현 (세션 관리, 메시지 목록)
- [x] 라우터에 챗봇 페이지 추가

#### 완료 기준
- [x] 채팅 UI에서 질문 → 답변 표시
- [x] 출처 카드 클릭 → 연설문 상세 페이지 이동
- [x] 모바일에서 채팅 UI 정상 작동

---

### Week 5: 챗봇 고도화 (SSE 스트리밍)

> 목표: SSE 실시간 스트리밍 UI + 대화 히스토리

- [x] `useSSE.js` composable 구현
  - EventSource API로 SSE 연결
  - `token` 이벤트 → 메시지에 점진적 추가
  - `sources` 이벤트 → 출처 카드 렌더링
  - `done` 이벤트 → 스트리밍 종료 처리
- [x] 타이핑 효과 구현 (CSS 애니메이션)
- [x] "새 대화" 버튼 구현 (세션 초기화)
- [x] 대화 히스토리 사이드 패널
  - 모바일: Sheet (슬라이드 인)
  - 데스크톱: Sidebar
- [x] 로딩 인디케이터 (AI 답변 생성 중)
- [x] 에러 처리 (네트워크 오류, 타임아웃)

#### 완료 기준
- [x] 질문 입력 → 답변이 한 글자씩 스트리밍 출력
- [x] 대화 히스토리 사이드 패널 정상 작동
- [x] 모바일에서 타이핑 애니메이션 부드러움

---

### Week 6: 키워드 대시보드 & 차트

> 목표: 연설문 통계 시각화 (워드클라우드, 차트)

- [x] Apache ECharts 설치 및 설정
- [x] `HomeView.vue` 대시보드 리뉴얼 (Linear 스타일)
  - 히어로 섹션 (AI 질문 입력창)
  - 통계 요약 카드 4개 (총 연설문, 주요 키워드, AI 질문 수, 평균 응답)
  - 차트 그리드
- [x] `StatCard.vue` 구현 (큰 숫자 + 트렌드 표시)
- [x] `WordCloud.vue` 구현 (ECharts wordCloud)
  - 키워드 클릭 → 관련 연설문 검색 페이지 이동
- [x] `LineChart.vue` 구현 (월별 연설 추이)
  - Linear 스타일 그라디언트 area
  - 6개월/1년 토글
- [x] `PieChart.vue` 구현 (카테고리별 분포)
  - 범례 리스트 (Linear 스타일)
- [x] `statsApi.js` 작성
- [x] `statsStore.js` 구현
- [x] 반응형 차트 (모바일: 1열, 태블릿 이상: 2열 그리드)
- [x] 터치 제스처 지원 (차트 확대/축소)

#### 완료 기준
- [x] 대시보드에 TOP 20 키워드 워드클라우드 표시
- [x] "AI" 클릭 → 월별 트렌드 차트 표시
- [x] 월별 연설 개수 라인 차트 정상 렌더링
- [x] 모바일에서 차트가 화면 너비에 맞게 반응형 렌더링

---

### Week 7: 카테고리 필터 UI

> 목표: 카테고리 필터를 챗봇/검색/대시보드에 통합

- [x] `CategoryFilter.vue` 구현
  - 모바일: 가로 스크롤 칩 (Badge)
  - 데스크톱: 전체 표시
- [x] 챗봇 페이지에 카테고리 필터 추가
  - 카테고리 선택 → 해당 분야만 검색하여 답변
- [x] 검색 페이지 카테고리 필터 연동
- [x] 대시보드 파이 차트 데이터 연동 (실제 API)
- [x] 연설문 상세 페이지에 카테고리 Badge 표시

#### 완료 기준
- [x] 챗봇에서 카테고리 선택 후 질문 가능
- [x] 검색 페이지에서 카테고리 필터 동작
- [x] 파이 차트로 카테고리 분포 표시
- [x] 모바일에서 카테고리 칩 가로 스크롤

---

### Week 8: 최적화 & 마무리

> 목표: 반응형 최종 점검 + 성능 최적화 + 다크모드

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
- [x] 모바일 화면에서 모든 기능 정상 작동
- [x] Lighthouse 모바일 점수 90+ 목표
- [x] 다크모드 전환 정상 작동
- [x] 빌드 후 코드 스플리팅 확인

---

## 6. 참고 사항

### 개발 환경 사전 준비
- Node.js 18+ 설치
- IDE: VS Code

### 디자인 레퍼런스
- [Linear.app](https://linear.app/) - 대시보드 및 전체 UI
- 색상: 회색조 기반 + 포인트 컬러 (#5E6AD2)
- 폰트: Inter + Pretendard
