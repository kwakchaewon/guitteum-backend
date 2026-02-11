---
name: commit
description: Conventional Commits 형식 자동 커밋
disable-model-invocation: true
allowed-tools: Bash
---

`git status`·`git diff` 병렬 실행. 변경 없으면 종료.
민감파일(`.env`,`credentials`,`*.key`,`*.pem`,`application-prod.yml`) 제외·경고.
나머지 개별 `git add` → `git commit -m "<type>: <한글 subject>"`.
type: feat|fix|docs|style|refactor|test|chore|build|ci|perf.
subject: 한글 30자 이내, 마침표 없음, 명사형 종결.
복잡한 변경만 AskUserQuestion 확인. push·amend·--no-verify 금지.
