---
name: commit
description: Conventional Commits 형식 자동 커밋
disable-model-invocation: true
allowed-tools: Bash
---

`git status`와 `git diff`를 병렬 실행하여 변경사항을 파악한다. 없으면 종료.

`.env`, `credentials`, `*.key`, `*.pem`, `application-prod.yml`은 스테이징 제외하고 경고. 나머지는 개별 `git add`.

커밋 메시지 형식: `<type>(<scope>): <subject>`
- type: feat|fix|docs|style|refactor|test|chore|build|ci|perf
- scope: speech, chat, keyword, batch, infra, global, db, config (여러 도메인이면 생략)
- subject: 영문 imperative, 소문자 시작, 50자 이내, 마침표 없음

메시지를 사용자에게 보여주고 AskUserQuestion으로 확인 후 커밋:

```bash
git commit -m "$(cat <<'EOF'
<메시지>

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

push 금지. --amend 금지. --no-verify 금지.
