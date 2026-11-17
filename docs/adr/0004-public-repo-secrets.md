# 0004. public 저장소 운영과 비밀값 관리

- 상태: 채택
- 날짜: 2026-11-07

## 배경

저장소를 public으로 운영한다. 커밋 기록은 누구나 볼 수 있고, 한 번 공개된 내용은 완전히 지우기 어렵다.

## 결정

- 라이선스 파일은 두지 않고 README에 All rights reserved를 명시한다. 코드는 공개하지만 재사용은 허락하지 않는다.
- 비밀값은 첫날부터 `.gitignore`로 막고 `local.properties`·환경변수로 주입한다.
- AI 도구가 비밀 파일을 읽지 못하도록 `.claude/settings.json`에 차단 규칙을 둔다.
- 커밋 작성자 이메일은 GitHub noreply 주소를 쓴다. 커밋 기록에서 이메일을 자동 수집하는 스팸 경로를 막기 위해서다.
- 개인 환경 정보는 `CLAUDE.local.md`(커밋 안 됨)에 둔다.

## 고려한 대안

- private 저장소 — 노출 위험은 없지만 코드를 공개할 수 없다.
- MIT 등 오픈소스 라이선스 — 재사용을 허락하게 된다. 지금은 허락하지 않기로 했다.

## 결과

- 실수로 비밀값을 커밋하는 것을 `.gitignore` 한 겹으로만 막고 있다. gitleaks, GitHub Push protection으로 보강할 예정이다 — `docs/engineering-checklist.md` 참고.
