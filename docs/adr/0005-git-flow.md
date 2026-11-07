# 0005. 브랜치 전략: Git Flow

- 상태: 채택
- 날짜: 2026-11-07

## 배경

모바일 앱은 버전 단위로 스토어에 배포하고, 배포 전에 심사 기간이 있다. 심사 중에도 다음 기능을 개발하고, 출시된 버전의 버그는 개발 중인 기능과 섞지 않고 고칠 수 있어야 한다. 실무(특히 국내 앱 팀)에서 많이 쓰는 방식을 익히는 것도 목적이다.

## 결정

Git Flow를 쓴다.

| 브랜치 | 갈라지는 곳 → 합치는 곳 | 용도 |
|---|---|---|
| `main` | — | 출시된 코드만. 출시마다 `vX.Y.Z` 태그 |
| `develop` | — | 다음 출시를 위한 개발 통합 브랜치. 기본 브랜치 |
| `feature/*` | `develop` → `develop` | 기능, 문서, 설정 변경 |
| `bugfix/*` | `develop` → `develop` | 아직 출시되지 않은 코드의 버그 |
| `release/X.Y.Z` | `develop` → `main` + `develop` | 출시 준비(버전 번호, 막바지 수정). 새 기능 금지 |
| `hotfix/X.Y.Z` | `main` → `main` + `develop` | 출시된 버전의 긴급 수정 |

- 모든 변경은 PR로 합친다. `main`·`develop`에 직접 커밋하지 않는다.
- `feature/*`, `bugfix/*` → `develop`은 **Squash merge**한다. PR 하나가 커밋 하나가 된다.
- `release/*`, `hotfix/*` → `main`, 그리고 `develop`으로 되돌려 합칠 때는 **Merge commit**을 쓴다. Squash하면 `main`과 `develop`의 이력이 어긋나 다음 합치기에서 충돌이 난다.
- `main`과 `develop`은 GitHub Ruleset으로 삭제와 force push를 막는다.

## 고려한 대안

- GitHub Flow(`main` + 작업 브랜치) — 더 단순하지만, 심사 중 병행 개발과 출시 버전 핫픽스를 브랜치로 구분할 수 없다. 첫 출시 때 Git Flow로 전환하는 방안도 있었지만, 처음부터 익숙해지는 쪽을 택했다.
- Trunk-based(짧은 브랜치 + feature flag) — 혼자 개발하는 초기에는 flag 관리 비용이 이점보다 크다.

## 결과

- 첫 출시 전까지는 `main`이 초기 커밋에 머물고, 모든 작업은 `develop`에 쌓인다.
- 출시마다 `release → main`, `release → develop` 두 번 합쳐야 한다.
- CI를 붙이면 `main`·`develop` Ruleset에 PR 필수와 빌드·테스트 통과 조건을 추가한다 — `docs/engineering-checklist.md` 참고.
