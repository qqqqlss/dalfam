# 달팸 (dalfam) 프로젝트 규칙

생리 주기 기록·예정일 알림 앱. Kotlin Multiplatform + Compose Multiplatform (Android 우선, iOS는 Mac 확보 후).

> 이 파일은 public 저장소에 공개된다. 개인 환경·계정·서버 주소는 `CLAUDE.local.md`(커밋 안 됨)에 둔다.
> 코드나 `libs.versions.toml`을 보면 알 수 있는 내용은 여기 적지 않는다. 결정의 이유는 `docs/adr/`에 둔다.

## 모듈

| 경로 | 역할 |
|---|---|
| `androidApp/` | Android 진입점(Activity, 매니페스트, 앱 리소스)만. 로직 금지 |
| `iosApp/` | iOS 진입점(Xcode 프로젝트) |
| `shared/` | Compose UI, 도메인 로직, 데이터 계층, 서버와 공유할 DTO |
| `server/` | (예정) Kotlin + Spring Boot |

## 반드시 지킬 것

### 공통 코드
- `commonMain`은 순수 Kotlin만 쓴다. `java.*`, `android.*` import 금지 — iOS에서 컴파일되지 않고, 순수 Kotlin 학습이 목적이다.
- 날짜·시간은 `kotlinx-datetime`만 쓴다(`java.time` 금지). 생리일은 시각이 아닌 날짜이므로 `LocalDate`를 기본으로 한다.
- 플랫폼 기능이 필요하면 `expect`/`actual` 또는 인터페이스 + Koin 주입으로 분리한다.
- 서버와 주고받을 DTO는 `shared`에 `@Serializable`로 둔다. DTO와 DB 엔티티·도메인 모델은 섞지 않는다.

### 건강 데이터 (민감정보)
- 생리 기록은 기기 로컬에만 저장한다. 사용자 동의와 ADR 없이 외부 전송 코드를 추가하지 않는다.
- 로그, 크래시 리포트, 애널리틱스 이벤트에 건강 데이터(날짜, 증상, 메모 등)를 넣지 않는다.
- 서버를 붙이면 AWS 서울 리전(ap-northeast-2)만 쓴다.

### 비밀값
- 키스토어, `.env`, `application-local.yml`, API 키, 서명 정보는 커밋하지 않는다(`.gitignore` 참고).
- 비밀값은 `local.properties` 또는 환경변수로 주입하고, 코드·빌드 스크립트에 하드코딩하지 않는다.
- 예시가 필요하면 `.env.example`처럼 값 없는 템플릿만 커밋한다.

### 의존성
- Kotlin·라이브러리·플러그인 버전은 `gradle/libs.versions.toml`에서만 관리한다. `build.gradle.kts`에 버전 문자열을 직접 쓰지 않는다.
- 새 라이브러리는 KMP(commonMain) 지원 여부를 먼저 확인한다.

## 명령

```bash
./gradlew :androidApp:assembleDebug        # Android 디버그 빌드
./gradlew :shared:testAndroidHostTest      # 공통 로직 테스트(JVM 호스트)
```

## 브랜치 (Git Flow — ADR 0005)
- 기본 브랜치는 `develop`. 작업은 `develop`에서 `feature/짧은-설명`(버그는 `bugfix/`)을 만들어 한다.
- `main`·`develop`에 직접 커밋·push하지 않는다. PR로만 합친다.
- `feature/*`·`bugfix/*` → `develop`: Squash merge. `release/*`·`hotfix/*` → `main`·`develop`: Merge commit.
- 출시는 `release/X.Y.Z`, 출시 버전 긴급 수정은 `main`에서 `hotfix/X.Y.Z`. 둘 다 `main`과 `develop` 양쪽에 합치고 `main`에 `vX.Y.Z` 태그.
- 작업 시작 전 현재 브랜치를 확인하고, `main`·`develop`이면 먼저 브랜치를 만든다.

## 작업 방식
- 되돌리기 어렵거나 구조에 영향을 주는 결정은 `docs/adr/`에 기록한다(템플릿: `docs/adr/0000-template.md`).
- 아직 정하지 않은 규칙과 도입 예정 도구는 `docs/engineering-checklist.md`에서 관리한다. 정해지면 이 파일로 옮긴다.

## 아직 정하지 않음 (TODO)
- 패키지·계층 구조 (예: `feature/`, `data/`, `domain/`)
- 상태 관리·화면 구조 (ViewModel, 내비게이션)
- 테스트 작성 기준
- 커밋 메시지 형식과 언어
