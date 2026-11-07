# 0007. 비동기는 코루틴, UI 상태는 단방향 데이터 흐름(UDF)

- 상태: 채택
- 날짜: 2026-11-07

## 배경

DB 읽기·쓰기, 설정 저장, 네트워크는 모두 시간이 걸리는 작업이라 메인 스레드를 막으면 안 된다. 이 앱이 쓰는 Room 3, DataStore, Ktor는 모두 `suspend`와 `Flow`를 기본 API로 제공한다. 화면은 그 결과를 일관된 상태로 그려야 한다.

## 결정

### 1. 비동기 처리는 코루틴만 쓴다

| 항목 | 규칙 |
|---|---|
| 한 번 받는 결과 | `suspend fun` (저장, 삭제, 단건 조회) |
| 계속 바뀌는 데이터 | `Flow` (기록 목록, 설정값) |
| 스레드 전환 | `data` 계층이 책임진다. Repository 구현이 `withContext`로 전환하고, 호출하는 쪽은 그냥 부른다(main-safe) |
| Dispatcher | 코드에 `Dispatchers.IO`를 직접 쓰지 않고 Koin으로 주입한다. 테스트에서 테스트용 Dispatcher로 바꾸기 위해서다 |
| Scope | ViewModel은 `viewModelScope`만 쓴다. `GlobalScope`와 운영 코드의 `runBlocking`은 금지한다 |
| 취소 | `CancellationException`을 삼키지 않는다. `runCatching`은 취소까지 잡으므로 코루틴 안에서 쓰지 않는다 |
| 테스트 | `kotlinx-coroutines-test`의 `runTest`, Flow 검증은 Turbine |

### 2. 화면은 UDF로 만든다

- 화면 상태는 `data class XxxUiState` 하나로 표현한다.
- 사용자 행동은 `sealed interface XxxAction`으로 표현하고 ViewModel의 `onAction(action)` 하나로 받는다.
- ViewModel은 `StateFlow<XxxUiState>`를 내보낸다. Flow를 상태로 바꿀 때는 `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 초기값)`을 쓴다.
- Compose는 `collectAsStateWithLifecycle()`로 받는다. 화면이 보이지 않을 때 수집을 멈춘다.
- 일회성 이벤트(스낵바, 화면 이동)도 가능하면 상태로 표현한다. 꼭 필요할 때만 `Channel`을 쓴다.

```kotlin
data class CycleUiState(
    val cycles: List<Cycle> = emptyList(),
    val nextPeriod: LocalDate? = null,
    val isLoading: Boolean = true,
)

sealed interface CycleAction {
    data class RecordStart(val date: LocalDate) : CycleAction
}

class CycleViewModel(
    observeCycles: ObserveCyclesUseCase,
    private val recordStart: RecordPeriodStartUseCase,
) : ViewModel() {
    val state: StateFlow<CycleUiState> = observeCycles()
        .map { CycleUiState(cycles = it, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CycleUiState())

    fun onAction(action: CycleAction) {
        when (action) {
            is CycleAction.RecordStart -> viewModelScope.launch { recordStart(action.date) }
        }
    }
}
```

데이터는 위(ViewModel)에서 아래(화면)로, 행동은 아래에서 위로만 흐른다.

## 고려한 대안

### 비동기

| 대안 | 택하지 않은 이유 |
|---|---|
| (올드) 콜백, `AsyncTask` | 콜백이 중첩되고 취소·에러 처리가 흩어진다. `AsyncTask`는 Android에서 삭제됐다 |
| RxJava | 강력하지만 연산자가 많아 학습 비용이 크고, KMP를 지원하지 않는다. Room 3는 코루틴 API만 제공한다 |

### UI 상태

| 대안 | 택하지 않은 이유 |
|---|---|
| (올드) MVP | Presenter가 View 인터페이스를 직접 호출한다. Compose의 "상태가 UI를 결정한다"는 방식과 맞지 않는다 |
| (올드) 값마다 `LiveData` | 로딩·데이터·에러가 따로 있어 서로 어긋난 상태가 생길 수 있다. `LiveData`는 Android 전용이라 commonMain에서 쓸 수 없다 |
| MVI 프레임워크 (Orbit, MVIKotlin) | 규칙이 엄격해 좋지만, 이 규모에서는 `StateFlow` + `sealed interface`만으로 같은 효과를 낼 수 있다 |

## 결과

- 화면은 항상 일관된 상태 하나만 그린다. 상태 변화를 테스트하기 쉽다(`state`를 Turbine으로 검증).
- Dispatcher를 주입하므로 테스트가 실제 스레드에 의존하지 않는다.
- iOS도 UI를 Compose로 공유하므로 Swift에서 코루틴을 직접 다룰 일이 거의 없다. Swift에서 호출해야 하면 SKIE나 Swift export(Kotlin 2.4 기준 알파)를 검토한다.
