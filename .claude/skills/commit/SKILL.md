---
name: commit
description: 변경사항을 분석하여 Conventional Commits 형식으로 자동 커밋
disable-model-invocation: true
allowed-tools: Bash
---

# Auto Commit

변경사항을 분석하고 Conventional Commits 형식의 커밋 메시지를 생성하여 커밋한다.

## 워크플로우

아래 단계를 순서대로 수행한다.

### 1. 변경사항 확인

다음 명령어를 **병렬로** 실행하여 현재 상태를 파악한다:

- `git status` — 변경/추가된 파일 목록 확인 (절대 `-uall` 플래그 사용 금지)
- `git diff` + `git diff --staged` — staged/unstaged 변경 내용 확인
- `git log --oneline -5` — 최근 커밋 히스토리 참고

변경사항이 없으면 **"커밋할 변경사항이 없습니다."** 라고 출력하고 종료한다.

### 2. 파일 스테이징

변경된 파일을 **개별적으로** `git add`한다.

**절대 스테이징하지 않을 파일:**
- `.env`, `.env.*`
- `*credentials*`, `*secret*`
- `*.key`, `*.pem`
- `application-prod.yml`

위 파일이 변경 목록에 있으면 스테이징에서 제외하고 사용자에게 경고한다.

### 3. 커밋 메시지 생성

staged 변경사항을 분석하여 아래 형식의 커밋 메시지를 작성한다:

```
<type>(<scope>): <subject>

<body (선택)>
```

#### type 선택 기준

| type | 용도 |
|------|------|
| `feat` | 새 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 변경 |
| `style` | 코드 포맷팅 (로직 변경 없음) |
| `refactor` | 리팩토링 (기능 변경 없음) |
| `test` | 테스트 추가/수정 |
| `chore` | 빌드, 설정 등 기타 |
| `build` | 빌드 시스템, 의존성 변경 |
| `ci` | CI 설정 변경 |
| `perf` | 성능 개선 |

#### scope 결정 규칙

프로젝트 패키지 구조를 기반으로 결정한다:

- `speech`, `chat`, `keyword` — 도메인별
- `batch` — Spring Batch 관련
- `infra` — 외부 시스템 연동 (OpenAI, Qdrant, ES, Redis)
- `global` — 공통 설정, 예외 처리
- `db` — Flyway 마이그레이션
- `config` — 설정 파일
- 여러 도메인에 걸친 변경은 scope 생략 가능

#### subject 규칙

- 영문 imperative mood (동사 원형으로 시작)
- 50자 이내
- 첫 글자 소문자
- 마침표 없음

#### body 규칙 (선택)

- 변경 이유나 상세 내용이 필요할 때만 작성
- 한국어 또는 영어

### 4. 사용자 확인

생성된 커밋 메시지를 사용자에게 보여주고 AskUserQuestion으로 확인을 요청한다:
- 승인 시 → 커밋 실행
- 수정 요청 시 → 메시지 수정 후 재확인

### 5. 커밋 실행

HEREDOC 형식으로 커밋 메시지를 전달한다:

```bash
git commit -m "$(cat <<'EOF'
<커밋 메시지>

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

### 6. 결과 확인

`git status`를 실행하여 커밋이 정상적으로 완료되었는지 확인하고 결과를 사용자에게 보여준다.

## 주의사항

- push는 절대 하지 않는다. 커밋만 수행한다.
- pre-commit hook 실패 시 문제를 수정하고 **새 커밋을 생성**한다 (--amend 금지).
- `--no-verify` 등 hook 우회 옵션 사용 금지.
- git config 수정 금지.
