# 아키텍처 결정 기록 (ADR)

되돌리기 어렵거나 구조에 영향을 주는 결정과 그 이유를 남긴다. "무엇을"은 코드와 `CLAUDE.md`에, "왜"는 여기에.

## 작성법

1. `0000-template.md`를 복사해 다음 번호로 만든다 (`0005-제목.md`).
2. 결정을 바꿀 때는 기존 문서를 고치지 않고 새 ADR을 쓴 뒤, 이전 문서의 상태를 "NNNN으로 대체"로 바꾼다.

## 목록

| 번호 | 제목 | 상태 |
|---|---|---|
| [0001](0001-kotlin-multiplatform.md) | Kotlin Multiplatform + Compose Multiplatform, Android 우선 | 채택 |
| [0002](0002-local-only-health-data.md) | 생리 기록은 기기 로컬에만 저장 | 채택 |
| [0003](0003-monorepo-shared-dto.md) | 모노레포와 공유 DTO | 채택 |
| [0004](0004-public-repo-secrets.md) | public 저장소 운영과 비밀값 관리 | 채택 |
| [0005](0005-git-flow.md) | 브랜치 전략: Git Flow | 채택 |
