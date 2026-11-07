# 엔지니어링 체크리스트

아직 정하지 않은 규칙과 도입 예정 도구를 모아둔다. 처음부터 다 갖추지 않고, 필요해지는 시점에 하나씩 채운다.

- 항목을 끝내면 `[x]`로 바꾸고, 정해진 규칙은 `CLAUDE.md`로, 결정 이유는 `docs/adr/`로 옮긴다.
- "언제" 열은 도입 시점의 기준이다. 그 시점이 오면 이 목록부터 본다.

## 1단계 — 지금 (프로젝트 골격)

- [x] `.gitignore`에 비밀 파일 패턴 추가
- [x] `.gitattributes`로 줄바꿈 LF 고정
- [x] 커밋 이메일을 GitHub noreply로 설정 (이 저장소 로컬 설정)
- [x] `CLAUDE.md`, `CLAUDE.local.md`, `.claude/settings.json` (비밀 파일 읽기 차단)
- [x] ADR 템플릿과 초기 결정 기록 (0001~0004)
- [x] `.editorconfig` (들여쓰기·줄바꿈·인코딩)
- [x] GitHub 저장소: `main` 브랜치 보호 Ruleset (Restrict deletions, Block force pushes)
- [x] 브랜치 전략: Git Flow (ADR 0005), `develop` 브랜치 생성
- [x] GitHub 저장소: 기본 브랜치 `develop`, Ruleset 대상 `main`·`develop`
- [ ] GitHub 저장소: Squash·Merge commit 허용, 머지 후 브랜치 자동 삭제 — **직접 설정**
- [x] 아키텍처·비동기·라이브러리 결정 (ADR 0006~0008)
- [ ] GitHub 계정: Keep my email private, Block command line pushes that expose my email — **직접 설정**
- [ ] GitHub 저장소: Secret scanning, Push protection 켜기 — **직접 설정**
- [ ] 커밋 메시지 형식과 언어 정하기 (예: Conventional Commits, 한국어/영어)

## 2단계 — 첫 기능 코드를 쓰기 전

| 항목 | 내용 | 결과물 |
|---|---|---|
| [x] 패키지·계층 구조 | 계층별 Gradle 모듈, 모듈 안은 기능별 패키지 | ADR 0006 + `CLAUDE.md` |
| [x] 상태 관리·화면 구조 | UDF(UiState + Action), StateFlow, Navigation 3 | ADR 0007·0008 |
| [ ] Convention Plugin | `build-logic/`에 KMP 라이브러리 모듈 공통 설정 | `build-logic/` |
| [ ] `domain`·`data` 모듈 생성 | 의존 방향대로 `settings.gradle.kts`에 등록 | 모듈 |
| [ ] 라이브러리 추가 | coroutines, kotlinx-datetime, Koin + Compiler Plugin, Room 3, DataStore, kotlinx-serialization, Navigation 3 (안정 버전 확인) | `libs.versions.toml` |
| [ ] 테스트 라이브러리 | kotlinx-coroutines-test, Turbine | `libs.versions.toml` |
| [ ] 백업 정책 | `allowBackup`, `dataExtractionRules`로 건강 데이터를 자동 백업에서 뺄지, 내보내기 기능을 둘지 | ADR (0002 후속) |
| [ ] 템플릿 예제 코드 정리 | `Greeting`, `Platform` 등 샘플 제거 | 코드 |

## 3단계 — 코드가 쌓이기 시작하면

| 항목 | 내용 | 결과물 |
|---|---|---|
| [ ] 테스트 작성 기준 | 무엇을 꼭 테스트할지 (예: 주기 계산·예정일 로직은 필수) | `CLAUDE.md` |
| [ ] 코드 스타일 검사 | ktlint 또는 detekt 포맷 규칙 | Gradle 플러그인 |
| [ ] `commonMain`의 `java.*` 금지 강제 | detekt 금지 import 규칙 | detekt 설정 |
| [ ] CI | GitHub Actions: Android 빌드 + `:shared:testAndroidHostTest` | `.github/workflows/` |
| [ ] PR 필수·검사 통과 | `main`·`develop` Ruleset에 Require a pull request, Require status checks 추가 | Ruleset |
| [ ] 비밀값 검사 | gitleaks (pre-commit 또는 CI) | 설정 파일 |

## 4단계 — 배포를 준비할 때

| 항목 | 내용 | 결과물 |
|---|---|---|
| [ ] 버전 규칙 | `versionName`은 `X.Y.Z`(SemVer), `versionCode` 증가 방식, `main` 태그 `vX.Y.Z` | `CLAUDE.md` |
| [ ] 릴리스 서명 | 키스토어는 저장소 밖에 두고 `local.properties`·환경변수로 주입 | `build.gradle.kts` |
| [ ] 난독화 | `isMinifyEnabled`, R8 규칙 | `proguard-rules.pro` |
| [ ] 개인정보처리방침 | 민감정보(건강) 수집·보관 범위 명시 | 문서 |
| [ ] 크래시 리포트 도입 여부 | 도입 시 건강 데이터가 섞이지 않게 하는 방법 | ADR |

## 5단계 — iOS (Mac 확보 후)

| 항목 | 내용 | 결과물 |
|---|---|---|
| [ ] Apple Team ID 관리 | `Config.xcconfig`에 직접 쓰지 않고 커밋 안 되는 로컬 파일로 분리 | `.gitignore` + xcconfig |
| [ ] iOS 빌드·테스트 | `:shared:iosSimulatorArm64Test` 통과 확인 | — |
| [ ] Swift 연동 | Swift에서 Kotlin 코루틴을 직접 호출해야 하면 SKIE 또는 Swift export(정식 출시 후) 검토 | ADR |

## 6단계 — 서버를 붙일 때

| 항목 | 내용 | 결과물 |
|---|---|---|
| [ ] 서버 동기화 결정 | 동의 절차, 암호화, 보관·파기 정책 | ADR (0002 대체) |
| [ ] `server/` 모듈 | Spring Boot + kotlinx-serialization | 모듈 |
| [ ] DTO 모듈 분리 여부 | `shared`의 UI 의존성과 DTO 분리 | ADR (0003 후속) |
| [ ] 서버 비밀값 | DB 비밀번호·AWS 키는 `application-local.yml`과 환경변수로 | `server/CLAUDE.md` |
| [ ] 인프라 | AWS 서울 리전(ap-northeast-2) | ADR |
