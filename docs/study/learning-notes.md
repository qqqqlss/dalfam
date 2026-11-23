# 달팸 공부 노트

달팸을 만들면서 질문하고 알게 된 것을 모아 두는 노트입니다.

- **ADR**(`docs/adr/`)에는 "무엇을 왜 정했나"를 남깁니다. 프로젝트 기록이라 결정이 바뀌면 새 번호로 씁니다.
- **이 노트**에는 "그래서 나는 무엇을 배웠나"를 남깁니다. 내 공부용이라 언제든 고치고 덧붙입니다.
- 출처: ADR 0001~0008, 지금까지의 코드(`domain`, `data`, `shared`), 질문하며 나눈 대화.

### 노트 관리 규칙

반복해서 읽을 노트라 **같은 내용은 한 곳에만** 둡니다.

1. 질문의 답은 맞는 주제 섹션(1장 또는 2장)에 넣습니다. 이미 비슷한 항목이 있으면 새로 쓰지 않고 그 항목을 고치거나 보강합니다.
2. 4장 "질문 기록"에는 날짜, 질문, 답이 있는 섹션 링크만 남깁니다. 답 본문은 쓰지 않습니다.
3. 새 주제가 생기면 해당 장에 소제목을 만들고 아래 목차에도 추가합니다.

## 목차

1. [분야별 비교: 올드 방식 → 지금 선택](#1-분야별-비교-올드-방식--지금-선택)
   - [1-1. 플랫폼](#1-1-플랫폼-adr-0001)
   - [1-2. 아키텍처: 계층을 어떻게 나누나](#1-2-아키텍처-계층을-어떻게-나누나-adr-0006)
   - [1-3. UseCase를 어떻게 쓰나](#1-3-usecase를-어떻게-쓰나-adr-0006)
   - [1-4. 비동기](#1-4-비동기-adr-0007)
   - [1-5. UI 상태](#1-5-ui-상태-adr-0007)
   - [1-6. 라이브러리](#1-6-라이브러리-adr-0008)
   - [1-7. 협업과 운영](#1-7-협업과-운영-adr-0003-0004-0005)
2. [개념 정리](#2-개념-정리)
   - [아키텍처](#아키텍처) · [코루틴](#코루틴) · [UDF와 ViewModel](#udf와-viewmodel) · [Koin (DSL)](#koin-dsl)
   - [KMP](#kmp) · [Room 3](#room-3) · [테스트](#테스트) · [프로젝트 운영](#프로젝트-운영)
3. [ADR과 실제 코드가 다른 점](#3-adr과-실제-코드가-다른-점)
4. [질문 기록](#4-질문-기록)
5. [다음에 비교해 볼 것](#5-다음에-비교해-볼-것)

---

## 1. 분야별 비교: 올드 방식 → 지금 선택

> 읽는 법: **올드**는 예전 Android에서 흔히 쓰던 방식, **대안**은 지금도 쓸 만한 다른 선택지, **선택**은 달팸이 고른 것입니다.
> 달팸은 KMP라서 "Android 전용(JVM 전용)이면 `commonMain`에서 못 쓴다"는 이유가 자주 나옵니다.

### 1-1. 플랫폼 (ADR 0001)

| 구분 | 방식 | 특징 / 고르지 않은 이유 |
|---|---|---|
| 올드 | Android 네이티브만 | iOS를 낼 때 처음부터 다시 만들어야 함 |
| 대안 | Flutter, React Native | Kotlin 공부라는 목적과 맞지 않음 |
| 대안 | KMP + 플랫폼별 UI(SwiftUI) | Mac 없이는 iOS UI를 못 만들고, UI를 두 벌 관리해야 함 |
| **선택** | **KMP + Compose Multiplatform** | UI까지 공유. Windows에서도 대부분 작성·테스트 가능 |

### 1-2. 아키텍처: 계층을 어떻게 나누나 (ADR 0006)

| 구분 | 방식 | 특징 / 고르지 않은 이유 |
|---|---|---|
| 올드 | `app` 모듈 하나, Activity가 DB·네트워크까지 | 테스트 불가, 화면이 바뀌면 로직도 같이 바뀜 |
| 대안 | 모듈 하나 + 계층별 패키지(`domain/`, `data/`) | 흔한 입문 구조. 하지만 폴더만 나뉠 뿐 `domain`에서 Room을 import해도 빌드가 통과함 |
| 대안 | 모듈 하나 + 기능별 패키지 | 응집도는 좋지만 의존 방향을 강제하지 못함 |
| 대안 | 기능 × 계층 모듈(Now in Android) | 팀이 크거나 빌드가 느릴 때 이득. 혼자 하는 초기엔 설정 비용이 큼 |
| **선택** | **계층별 Gradle 모듈(`domain`/`data`/`shared`)** | 의존 방향을 어기면 **컴파일 에러**. 규칙을 사람이 아니라 컴파일러가 지킴 |

모듈 안 패키지는 **기능 → 계층** 순서입니다(`domain/cycle/usecase/...`).

| 구분 | 방식 | 특징 |
|---|---|---|
| 대안 | 계층별(`model/`, `usecase/`) | 파일이 적을 땐 간단. 기능이 늘면 한 폴더에 관계없는 파일이 쌓임 |
| 대안 | 기능별, 하위 폴더 없음 | 파일이 10개를 넘으면 찾기 어려움. 작은 기능엔 이 형태 그대로 씀 |
| **선택** | **기능 → 계층** | 기능 하나를 고칠 때 한 폴더 안에서 끝남 |

### 1-3. UseCase를 어떻게 쓰나 (ADR 0006)

| 구분 | 방식 | 특징 / 고르지 않은 이유 |
|---|---|---|
| 대안 | 로직이 있을 때만 UseCase (Google 가이드: domain 계층은 선택) | 실용적이지만 "이건 UseCase가 필요한가"를 매번 판단해야 함 |
| 대안 | UseCase 없이 Repository만 (계층형 MVVM) | 계산 로직이 ViewModel에 쌓임 |
| 대안 | 기능별 묶음 클래스(`CycleInteractor`) | 클래스가 커지고 의존성이 늘어남 |
| **선택** | **ViewModel은 항상 UseCase를 거침** | 기준이 단순함. 대신 값을 전달만 하는 UseCase도 생김 |

### 1-4. 비동기 (ADR 0007)

| 구분 | 방식 | 특징 / 고르지 않은 이유 |
|---|---|---|
| 올드 | 콜백, `AsyncTask` | 콜백 중첩, 취소·에러 처리가 흩어짐. `AsyncTask`는 삭제됨 |
| 대안 | RxJava | 강력하지만 학습 비용이 크고 KMP 미지원. Room 3는 코루틴 API만 제공 |
| **선택** | **코루틴** (`suspend` = 한 번 받는 결과, `Flow` = 계속 바뀌는 데이터) | Room 3·DataStore·Ktor가 모두 코루틴 기반 |

### 1-5. UI 상태 (ADR 0007)

| 구분 | 방식 | 특징 / 고르지 않은 이유 |
|---|---|---|
| 올드 | MVP | Presenter가 View를 직접 호출. "상태가 UI를 결정한다"는 Compose 방식과 안 맞음 |
| 올드 | 값마다 `LiveData` | 로딩·데이터·에러가 따로 있어 서로 어긋날 수 있음. Android 전용 |
| 대안 | MVI 프레임워크(Orbit, MVIKotlin) | 규칙이 엄격해 좋지만 이 규모엔 과함 |
| **선택** | **UDF: `UiState` 하나 + `StateFlow`** | 화면은 항상 일관된 상태 하나만 그림. 테스트하기 쉬움 |

### 1-6. 라이브러리 (ADR 0008)

| 분야 | 올드 | 대안 | **선택** |
|---|---|---|---|
| 로컬 DB | `SQLiteOpenHelper` 직접 사용 (SQL·변환을 손으로, 컴파일 검사 없음) | Room 2.x, SQLDelight(SQL 먼저 쓰고 코드 생성), Realm(앞날 불투명) | **Room 3** — KMP 중심, KSP 전용, 모든 API가 코루틴 |
| DI | 직접 생성·싱글턴 (테스트에서 가짜로 바꾸기 어려움) | Dagger 2 / Hilt(실무 표준, KMP 미지원), Metro(컴파일 타임, KMP 지원, 1순위 대안), kotlin-inject | **Koin** (+ Compiler Plugin 예정) |
| 화면 이동 | Fragment + Navigation XML | Navigation Compose 2.x(백스택을 라이브러리가 숨김), Voyager, Decompose | **Navigation 3** — 백스택을 내가 가진 리스트로 관리 |
| 설정 저장 | `SharedPreferences` (동기 API, 메인 스레드를 막을 수 있음, Android 전용) | — | **DataStore** |
| 네트워크 | — | Retrofit + OkHttp (실무 표준, JVM 전용) | **Ktor Client** (+ Retrofit 같은 선언 방식이 필요하면 Ktorfit) |
| 직렬화 | Gson (Kotlin null 안전성 무시) | Moshi (JVM 전용) | **kotlinx-serialization** |
| 날짜 | `Date`, `Calendar` | `java.time` (JVM 전용) | **kotlinx-datetime** + `kotlin.time.Instant` |
| 테스트 대역 | Mockito (JVM 전용) | MockK (KMP 지원 제한적) | **Fake 객체** (Google도 권장), 꼭 필요하면 Mokkery |

### 1-7. 협업과 운영 (ADR 0003, 0004, 0005)

| 분야 | 대안 | **선택** | 이유 |
|---|---|---|---|
| 브랜치 전략 | GitHub Flow(단순), Trunk-based(feature flag 관리 비용) | **Git Flow** | 스토어 심사 중 병행 개발, 출시 버전 핫픽스를 브랜치로 구분 |
| 머지 방식 | — | `feature/*` → `develop`은 **Squash**, `release`/`hotfix` → `main`은 **Merge commit** | release를 Squash하면 `main`과 `develop` 이력이 어긋나 다음 머지에서 충돌 |
| 저장소 구성 | 앱·서버 분리, OpenAPI 코드 생성 | **모노레포 + 공유 DTO** | DTO를 바꾸면 앱·서버가 같은 커밋에서 같이 컴파일되어 불일치를 바로 발견 |
| 데이터 저장 위치 | 처음부터 서버 동기화 | **기기 로컬만** | 건강 정보는 민감정보. 서버에 두면 동의·암호화·파기·유출 대응 책임이 생김 |

---

## 2. 개념 정리

### 아키텍처

- **의존성은 안쪽(비즈니스 규칙)으로만 향한다.** 클린 아키텍처의 핵심 규칙 하나.
  `androidApp → shared → domain`, `shared → data → domain`. `domain`은 아무것도 모릅니다.
- **의존성 역전**: `domain`이 `PeriodRepository` **인터페이스**를 정하고, `data`가 `PeriodRepositoryImpl`로 **구현**합니다. 그래서 `domain`은 Room을 몰라도 됩니다.
- **`internal`로 구현 숨기기**: `PeriodRepositoryImpl`, `PeriodDao`, `DalfamDatabase`는 전부 `internal`입니다. `data` 밖에서는 Koin 모듈(`dataModule`)과 인터페이스로만 씁니다.
- **패키지는 의존을 막지 못한다.** Kotlin에는 package-private이 없습니다. 패키지는 찾기 쉽게 하는 용도이고, 의존을 막는 건 모듈의 몫입니다.
- **계층마다 자기 모델을 쓴다**: `PeriodEntity`(data, Room) ↔ `Period`(domain) ↔ `HomeUiState`(shared). 변환 함수(`toDomain()`, `toEntity()`)는 바깥 계층인 `data`에 둡니다.

### 코루틴

- **main-safe**: 스레드 전환은 `data`가 책임집니다. `PeriodRepositoryImpl`이 `withContext(ioDispatcher)`·`flowOn(ioDispatcher)`로 바꾸고, 부르는 쪽은 그냥 부릅니다.
- **Dispatcher 주입**: `Dispatchers.IO`를 코드에 직접 쓰지 않고 Koin으로 넣어 줍니다. 테스트에서 테스트용 Dispatcher로 바꾸기 위해서입니다.
- **`viewModelScope`**: ViewModel이 버려지면 그 안의 코루틴이 자동으로 취소됩니다. `GlobalScope`와 운영 코드의 `runBlocking`은 금지입니다.
- **`runCatching`은 코루틴 안에서 쓰지 않는다**: `CancellationException`(취소 신호)까지 잡아 버려서 취소가 제대로 안 됩니다.

### UDF와 ViewModel

```
화면 --(onStartClick 같은 이벤트)--> ViewModel --> UseCase --> DB
  ^                                                         |
  +------------------(uiState: StateFlow)-------------------+
```

- 데이터는 위에서 아래로, 이벤트는 아래에서 위로만 흐릅니다. 화면은 상태를 직접 바꾸지 않습니다.
- **`MutableStateFlow`는 안에, `StateFlow`는 밖에**: 바꾸는 건 ViewModel 안에서만 합니다.
- **`stateIn(viewModelScope, WhileSubscribed(5_000), 초기값)`**: 차가운 Flow를 "항상 최신 값 하나를 들고 있는" StateFlow로 바꿉니다. 화면이 안 보이면 5초 뒤 DB 구독을 멈춥니다. 화면 회전처럼 잠깐 끊길 때 다시 구독하지 않으려고 5초 여유를 둡니다.
- **`combine`**: DB 기록 Flow와 안내 메시지 Flow를 합쳐 `HomeUiState` 하나로 만듭니다.
- **UiState는 불변(`data class` + `val`)**: 바뀔 때는 새 객체를 만듭니다. 그래서 Compose가 "이전 값과 다르다"를 쉽게 알아챕니다.
- **일회성 이벤트(스낵바)도 상태로**: `message`를 상태에 두고, 보여 준 뒤 `onMessageShown()`으로 지웁니다. 안 지우면 화면 회전 때 같은 안내가 또 뜹니다.
- **ViewModel은 문자열을 모른다**: `HomeMessage` enum만 넘기고, 실제 문구(한국어/영어)는 화면이 string resource에서 고릅니다. 다국어 처리가 쉬워지고 ViewModel 테스트도 문구와 무관해집니다.
- **`collectAsStateWithLifecycle()`**: 화면이 안 보일 때 수집을 멈춥니다.

### Koin (DSL)

| 키워드 | 의미 | 달팸에서 |
|---|---|---|
| `single { }` | 앱 전체에 하나 | DB, DAO, Repository, Dispatcher, `DateProvider` |
| `factory { }` | 꺼낼 때마다 새로 | UseCase(상태가 없으니 매번 만들어도 됨) |
| `viewModel { }` | 화면 생명주기에 맞춰 보관 | `HomeViewModel`. 화면 회전에도 유지, 화면이 닫히면 버려짐 |
| `named("io")` | 같은 타입이 여럿일 때 붙이는 이름표(qualifier) | `CoroutineDispatcher`는 Default·Main 등 여럿이라 `IoDispatcher`로 구분 |

- **인터페이스 타입으로 등록해야 인터페이스로 찾을 수 있다**: `single<PeriodRepository> { PeriodRepositoryImpl(...) }`.
- **Koin은 정확한 타입으로 찾는다**: 파라미터 타입이 `CoroutineContext`면 그냥 `get()`은 `CoroutineContext`를 찾습니다. 등록한 타입인 `get<CoroutineDispatcher>(IoDispatcher)`로 명시해야 합니다.
- **생성자 기본값은 Koin이 쓰지 않는다**: 필요한 것은 전부 `get()`으로 넘깁니다.
- **`domain`은 Koin을 모른다**: `domain` 등록(`domainModule`)은 `shared`에서 합니다. `domain`을 순수 Kotlin으로 두기 위해서입니다.
- **시작은 한 번만**: Android는 `Application.onCreate()`(`DalfamApp`)에서 `initKoin { androidContext(...) }`를 부릅니다.
- **모듈 검증 테스트**: `appModules` 목록을 테스트에서도 그대로 써서 등록이 빠진 게 없는지 확인합니다(`AppModulesTest`).

### KMP

- **`expect` / `actual`**: 공통 코드에 "이런 게 있다"(`expect`)고 선언하고, 플랫폼별로 실제 구현(`actual`)을 둡니다. 예: `platformModule`, DB 빌더.
- `DalfamDatabaseConstructor`처럼 **라이브러리 컴파일러가 `actual`을 생성**해 주는 경우도 있습니다(Room).
- **`commonMain`에서 `java.*` 금지**: iOS에서는 JVM이 없습니다. 그래서 `java.time` 대신 kotlinx-datetime을 씁니다.

### Room 3

- **DAO**: 계속 지켜볼 것은 `Flow`(`observeAll`), 한 번 하는 것은 `suspend`(`insert`, `getAll`).
- **`BundledSQLiteDriver`**: SQLite를 앱에 같이 묶어 Android·iOS가 같은 SQLite를 씁니다.
- **`setQueryCoroutineContext`**: 쿼리를 어느 Dispatcher에서 돌릴지 정합니다. 주입받은 IO Dispatcher를 넘깁니다.
- **`exportSchema = true`**: 스키마를 `data/schemas/`에 JSON으로 내보내 커밋합니다. 스키마를 바꾸면 `version`을 올리고 마이그레이션을 추가합니다.
- **`@ColumnTypeConverters`**: `LocalDate`처럼 DB가 모르는 타입을 저장할 수 있는 타입으로 바꿉니다.

### 테스트

- **Fake 객체**: `FakePeriodRepository`, `InMemoryPeriodRepository`처럼 인터페이스를 메모리로 구현해 씁니다. mock 라이브러리 없이도 됩니다.
- **`DateProvider`**: "오늘"을 주입받게 해서 테스트에서 날짜를 고정합니다.
- 코루틴은 `runTest`, Flow는 Turbine으로 검증합니다.

### 프로젝트 운영

- **ADR**(Architecture Decision Record, 아키텍처 결정 기록): 되돌리기 어렵거나 구조에 영향을 주는 결정을 "배경 / 결정 / 고려한 대안 / 결과"로 짧게 남기는 문서입니다. 번호(0006 등)는 일련번호입니다. 코드를 보면 "Room을 쓴다"는 건 알 수 있지만 "왜 SQLDelight가 아니라 Room인지"는 알 수 없어서, 그걸 남깁니다. 결정을 바꿀 때 기존 문서는 고치지 않고 새 번호로 쓴 뒤 "0012로 대체"라고 표시합니다. 한 결정에 한 문서가 원칙이라, 나중에 하나만 바꿀 때(예: Koin → Metro) 그 문서만 대체하면 됩니다.
- **public 저장소**: 비밀값은 첫날부터 `.gitignore`로 막고 `local.properties`·환경변수로 넣습니다. 커밋 이메일은 GitHub noreply 주소를 씁니다.

---

## 3. ADR과 실제 코드가 다른 점

공부하다 보면 "문서엔 이렇게 써 있는데 코드는 왜 다르지?"가 생깁니다. 발견하면 여기에 적고, 어느 쪽을 맞출지 정합니다.

| 항목 | ADR | 지금 코드 | 메모 |
|---|---|---|---|
| ViewModel 이벤트 받기 | `sealed interface XxxAction` + `onAction(action)` 하나 (0007) | `onStartClick()`, `onEndClick()`, `onMessageShown()` 함수 여러 개 | 버튼이 적을 땐 함수가 더 단순함. 화면이 커지면 `Action`으로 바꿀지 판단 |
| DI | Koin + **Compiler Plugin** (0008) | Koin **DSL**(`module { single { } }`) | 계획대로 DSL을 먼저 익히고, 다음 단계에서 Annotations로 바꿔 Hilt와 비교 |

---

## 4. 질문 기록

언제 무엇을 궁금해했는지 보는 색인입니다. 답은 링크된 섹션에 있습니다. 같은 질문을 다시 하면 새 줄을 만들지 않고 기존 줄에 날짜만 덧붙입니다.

| 날짜 | 질문 | 답이 있는 곳 |
|---|---|---|
| 2026-11-07 | ADR 0006의 "ADR"이 뭐야? | [프로젝트 운영](#프로젝트-운영) |

<!-- 새 질문은 표 맨 아래에 한 줄 추가: | YYYY-MM-DD | 질문 한 줄 | [섹션 이름](#앵커) | -->


---

## 5. 다음에 비교해 볼 것

학습 순서: Koin DSL ✅ → ViewModel·상태 ✅ → 다국어 ✅ → **Koin Annotations** → Navigation 3 → DataStore

| 다음 단계 | 무엇과 비교하나 |
|---|---|
| Koin Annotations | 지금의 Koin DSL, 그리고 실무 표준인 Hilt(`@Inject`, `@Module`) |
| Navigation 3 | Fragment + Navigation XML, Navigation Compose 2.x |
| DataStore | `SharedPreferences` |
