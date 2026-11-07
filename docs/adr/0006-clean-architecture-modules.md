# 0006. 클린 아키텍처: 계층별 Gradle 모듈과 UseCase

- 상태: 채택
- 날짜: 2026-11-07

## 배경

달팸의 핵심은 화면이 아니라 계산 규칙이다(주기 길이, 다음 예정일, 가임기 예측). 이 규칙은 DB를 Room에서 다른 것으로 바꾸거나 화면을 고쳐도 변하지 않아야 하고, 나중에 서버를 붙이면 서버에서도 같은 규칙을 쓸 수 있으면 좋다. 클린 아키텍처를 공부하는 것도 이 프로젝트의 목적이다.

클린 아키텍처의 핵심 규칙은 하나다. **의존성은 안쪽(비즈니스 규칙)으로만 향한다.** 이 규칙을 문서와 리뷰가 아니라 컴파일러가 지키게 하고 싶다.

## 결정

### 1. 계층마다 Gradle 모듈을 둔다

```
domain/      모델, Repository 인터페이스, UseCase
data/        Room, DataStore, Ktor, Repository 구현, Entity·DTO ↔ 도메인 모델 변환
shared/      Compose UI, ViewModel, Koin 조립, iOS Shared.framework 묶음
androidApp/  Android 진입점
build-logic/ 모듈 공통 빌드 설정(Convention Plugin)
```

의존 방향:

```
androidApp ──▶ shared ──▶ domain
                  │          ▲
                  └──▶ data ─┘
```

- `domain`은 다른 모듈에 의존하지 않는다. 외부 라이브러리는 `kotlinx-coroutines-core`, `kotlinx-datetime`만 허용한다. 둘 다 순수 Kotlin 멀티플랫폼 라이브러리라 "domain은 프레임워크를 모른다"는 원칙을 깨지 않는다.
- `domain`이 Repository **인터페이스**를 정의하고 `data`가 **구현**한다(의존성 역전). `domain`에서 Room을 import하면 `domain/build.gradle.kts`에 Room이 없으므로 컴파일 에러가 난다.
- `data`의 구현 클래스는 `internal`로 숨기고, 밖으로는 Koin 모듈(`dataModule`)만 공개한다. `shared`가 `data`에 의존하는 이유는 DI 조립뿐이다.
- 모듈 안의 패키지는 기능별로 나눈다(`cycle/`, `notification/`, `settings/`).
- 모듈마다 반복되는 KMP 타깃·Android 설정은 `build-logic/`의 Convention Plugin으로 한 곳에서 관리한다.

### 2. ViewModel은 항상 UseCase를 거친다

- ViewModel은 UseCase만 알고 Repository를 모른다. 단순 조회도 UseCase 하나를 만든다.
- UseCase 하나는 앱이 할 수 있는 일 하나다. 공개 함수는 `operator fun invoke` 하나만 둔다.
- 이름은 `동사 + 대상 + UseCase` (`ObserveCyclesUseCase`, `RecordPeriodStartUseCase`, `PredictNextPeriodUseCase`).

```kotlin
// domain
interface CycleRepository {
    fun observeCycles(): Flow<List<Cycle>>
    suspend fun save(cycle: Cycle)
}

class ObserveCyclesUseCase(private val repository: CycleRepository) {
    operator fun invoke(): Flow<List<Cycle>> = repository.observeCycles()
}

// data
internal class CycleRepositoryImpl(private val dao: CycleDao) : CycleRepository { ... }
```

### 3. 계층마다 자기 모델을 쓴다

| 모델 | 위치 | 예 |
|---|---|---|
| Entity | `data` (Room) | `CycleEntity` |
| DTO | `data` 또는 서버 공유 모듈 | `CycleDto` (`@Serializable`) |
| 도메인 모델 | `domain` | `Cycle` |
| UI 상태 | `shared` | `CycleUiState` |

변환 함수(`toDomain()`, `toEntity()`)는 바깥 계층에 둔다. `domain`은 다른 모델의 존재를 모른다.

## 고려한 대안

### 계층 분리 방법

| 대안 | 내용 | 택하지 않은 이유 |
|---|---|---|
| 모듈 하나 + 계층별 패키지 | `shared` 안에 `domain/`, `data/`, `presentation/` 패키지 | 입문 구조로 흔하다. 하지만 폴더만 나뉠 뿐 `domain`에서 Room을 import해도 빌드가 통과한다. Konsist 같은 아키텍처 테스트로 보완할 수 있지만 컴파일러만큼 즉각적이지 않다 |
| 모듈 하나 + 기능별 패키지 | `cycle/domain`, `cycle/data` ... | 기능 단위 응집도는 좋지만 의존 방향을 강제하지 못하는 건 같다. 이 장점은 모듈 안 패키지 구성으로 가져왔다 |
| 기능 × 계층 모듈 | `core/*`, `feature/*` 여러 모듈 (Now in Android 방식) | 여러 팀이 기능별로 동시에 작업하거나 빌드가 느릴 때 이득이다. 혼자 하는 초기에는 설정 비용이 이점보다 크다. 앱이 커지면 계층 모듈에서 이 구조로 나누기 어렵지 않다 |
| (올드) `app` 모듈 하나에 Activity가 모든 일 | 화면 코드에 DB·네트워크 호출 | 테스트할 수 없고, 화면이 바뀌면 로직도 같이 바뀐다 |

### UseCase 사용 방식

| 대안 | 내용 | 택하지 않은 이유 |
|---|---|---|
| 로직이 있을 때만 UseCase | 단순 조회는 ViewModel이 Repository를 직접 호출. Google 안드로이드 가이드는 domain 계층을 선택 사항으로 본다 | 파일이 적어 실용적이다. 하지만 "이건 UseCase가 필요한가"를 매번 판단해야 하고 기준이 흐려지기 쉽다 |
| UseCase 없이 Repository만 | 계층형 MVVM | 계산 로직이 ViewModel에 쌓인다. 엄밀히는 클린 아키텍처가 아니다 |
| 기능별 묶음 클래스(Interactor, Service) | `CycleInteractor`에 관련 동작을 모두 | 파일은 적지만 클래스가 커지고 의존성이 늘어난다 |

## 결과

- 의존 방향을 어기면 컴파일 에러가 난다. 구현은 `internal`로 숨겨진다.
- `domain` 모듈은 Compose·Android에 의존하지 않으므로 서버에서도 재사용할 수 있다.
- 감수하는 것: 모듈 세 개의 빌드 설정(Convention Plugin으로 줄인다), 값을 전달만 하는 UseCase, 계층마다 모델 변환 코드.
- 서버와 공유할 DTO를 어느 모듈에 둘지는 서버를 붙일 때 정한다(0003 후속). `data`에는 Room이 있으므로 서버가 의존하기에 적절하지 않다.
- 처음 써보고 과하다고 판단되는 부분이 있으면 새 ADR로 조정한다.
