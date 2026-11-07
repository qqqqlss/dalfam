# 달팸 (dalfam) 프로젝트 규칙

생리 주기 기록·예정일 알림 앱. Kotlin Multiplatform + Compose Multiplatform (Android 우선, iOS는 Mac 확보 후).

> 이 파일은 public 저장소에 공개된다. 개인 환경·계정·서버 주소는 `CLAUDE.local.md`(커밋 안 됨)에 둔다.
> 코드나 `libs.versions.toml`을 보면 알 수 있는 내용은 여기 적지 않는다. 결정의 이유는 `docs/adr/`에 둔다.

## 모듈

| 경로 | 역할 |
|---|---|
| `androidApp/` | Android 진입점(Activity, 매니페스트, 앱 리소스)만. 로직 금지 |
| `iosApp/` | iOS 진입점(Xcode 프로젝트) |
| `shared/` | Compose UI, ViewModel, Koin 조립, iOS 프레임워크 묶음 |
| `domain/` | (예정) 도메인 모델, Repository 인터페이스, UseCase. 순수 Kotlin |
| `data/` | (예정) Room·DataStore·Ktor, Repository 구현, 모델 변환 |
| `build-logic/` | (예정) 모듈 공통 빌드 설정(Convention Plugin) |
| `server/` | (예정) Kotlin + Spring Boot |

의존 방향: `androidApp → shared → domain`, `shared → data → domain`. `domain`은 아무것도 의존하지 않는다.

## 반드시 지킬 것

### 공통 코드
- `commonMain`은 순수 Kotlin만 쓴다. `java.*`, `android.*` import 금지 — iOS에서 컴파일되지 않고, 순수 Kotlin 학습이 목적이다.
- 날짜·시간은 `kotlinx-datetime`만 쓴다(`java.time` 금지). 생리일은 시각이 아닌 날짜이므로 `LocalDate`를 기본으로 한다.
- 플랫폼 기능이 필요하면 `expect`/`actual` 또는 인터페이스 + Koin 주입으로 분리한다.
- 서버와 주고받을 DTO는 `shared`에 `@Serializable`로 둔다. DTO와 DB 엔티티·도메인 모델은 섞지 않는다.

### 아키텍처 (ADR 0006)
- `domain`은 `kotlinx-coroutines-core`, `kotlinx-datetime` 외의 라이브러리에 의존하지 않는다. Room·Compose·Koin·Ktor 금지.
- Repository 인터페이스는 `domain`, 구현은 `data`에 `internal`로 둔다. `data`가 밖에 공개하는 것은 Koin 모듈뿐이다.
- ViewModel은 UseCase만 주입받는다. Repository를 직접 쓰지 않는다. 단순 조회도 UseCase를 만든다.
- UseCase는 공개 함수 `operator fun invoke` 하나만 가진다. 이름은 `동사 + 대상 + UseCase`.
- Entity(`data`), DTO, 도메인 모델(`domain`), UiState(`shared`)는 서로 다른 클래스다. 변환 함수는 바깥 계층에 둔다.
- 모듈 안의 패키지는 기능별로 나눈다(`cycle/`, `notification/`, `settings/`).

### 비동기·UI 상태 (ADR 0007)
- 한 번 받는 결과는 `suspend`, 계속 바뀌는 데이터는 `Flow`.
- 스레드 전환은 `data` 계층이 `withContext`로 책임진다. Dispatcher는 직접 쓰지 않고 주입받는다.
- `GlobalScope`, 운영 코드의 `runBlocking`, 코루틴 안의 `runCatching` 금지. `CancellationException`을 삼키지 않는다.
- 화면 상태는 `XxxUiState` 하나, 사용자 행동은 `sealed interface XxxAction` + `onAction()`. ViewModel은 `StateFlow`를 내보내고 Compose는 `collectAsStateWithLifecycle()`로 받는다.

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
- 정식(stable) 버전만 쓴다. 알파·베타는 ADR로 이유를 남긴 경우에만 쓴다. 라이브러리 선택은 ADR 0008을 따른다(Room 3, Koin + Compiler Plugin, Navigation 3).

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
- 테스트 작성 기준
- 커밋 메시지 형식과 언어
