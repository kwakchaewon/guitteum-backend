---
name: commit
description: Conventional Commits 형식 자동 커밋
disable-model-invocation: true
allowed-tools: Bash
---

`git status`와 `git diff`를 병렬 실행. 변경사항 없으면 종료.

`.env`, `credentials`, `*.key`, `*.pem`, `application-prod.yml`은 제외 후 경고. 나머지 개별 `git add`.

메시지: `<type>: <한글 subject>`
- type: feat|fix|docs|style|refactor|test|chore|build|ci|perf
- subject: **한글**, 30자 이내, 마침표 없음, 명사형 종결 (예: "연설문 검색 기능 추가")

단일 파일·단순 변경은 바로 커밋. 여러 파일·복잡한 변경은 AskUserQuestion으로 확인 후 커밋:

```bash
git commit -m "$(cat <<'EOF'
<메시지>
EOF
)"
```

push·amend·--no-verify 금지.
