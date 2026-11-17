# 0008. 라이브러리 선택과 최신 기술 도입 기준

- 상태: 채택
- 날짜: 2026-11-07
- 관련: 0001의 라이브러리 목록을 구체화한다

## 배경

가급적 최신 흐름을 따라가며 올드 방식과 비교해 공부하고 싶다. 하지만 최신 기술이 항상 안정적이지는 않고, KMP는 Android 전용 라이브러리를 쓸 수 없다. 무엇을 기준으로 고를지 정해둔다.

## 결정

### 도입 기준

1. **정식(stable) 버전만 쓴다.** 알파·베타는 이유를 ADR로 남긴 경우에만 쓴다.
2. **KMP(commonMain) 지원을 먼저 확인한다.** Android 전용이면 `androidMain`에서만 쓸 수 있는지 따져본다.
3. **새 기술을 도입할 때는 올드 방식 대비 나아진 점을 ADR에 적는다.**

### 선택 (2026-11 기준)

| 분야 | 선택 | 메모 |
|---|---|---|
| 로컬 DB | **Room 3** (`androidx.room3`) | 2026-07 정식. KMP 중심, KSP 전용, 모든 DB API가 코루틴 기반 |
| DI | **Koin + Koin Compiler Plugin** | 의존성 그래프를 컴파일 시점에 검사. Kotlin 2.4.20 이상은 플러그인 1.2.1 이상 |
| 화면 이동 | **Navigation 3** | 백스택을 직접 가진 리스트로 관리. Android 정식(2025-11), Compose Multiplatform 1.10부터 지원. 도입 시 멀티플랫폼 아티팩트의 안정 버전을 확인한다 |
| 설정 저장 | DataStore | |
| 네트워크 | Ktor Client (+ 필요 시 Ktorfit) | 서버를 붙일 때 |
| 직렬화 | kotlinx-serialization | |
| 날짜·시간 | kotlinx-datetime + 표준 라이브러리 `kotlin.time.Instant` | |
| 비동기 | kotlinx-coroutines | 0007 |
| 테스트 | kotlin.test, kotlinx-coroutines-test, Turbine, Fake 객체 | mock 라이브러리는 꼭 필요할 때 KMP용(Mokkery)으로 |

## 고려한 대안

| 분야 | 대안 | 택하지 않은 이유 |
|---|---|---|
| 로컬 DB | (올드) `SQLiteOpenHelper` 직접 사용 | SQL과 변환 코드를 손으로 써야 하고 컴파일 시점 검사가 없다 |
| | Room 2.x | KMP를 지원하지만 Room 3가 KMP 중심으로 정리된 후속 버전이다 |
| | SQLDelight | SQL을 먼저 쓰고 Kotlin 코드를 생성한다. 좋은 선택이지만 Android 실무에서 Room이 더 흔하다 |
| | Realm | Atlas Device Sync가 종료되며 앞날이 불투명하다 |
| DI | (올드) 직접 생성·싱글턴 | 테스트에서 가짜 객체로 바꾸기 어렵다 |
| | Dagger 2 / Hilt | Android 실무 표준이지만 KMP를 지원하지 않는다 |
| | Metro | 컴파일 타임 DI로 Dagger처럼 엄격하고 KMP를 지원한다. 떠오르는 선택지지만 학습 자료가 Koin보다 적다. Koin이 맞지 않으면 1순위 대안 |
| | kotlin-inject | KSP 기반 컴파일 타임 DI. Metro가 비슷한 장점을 더 적극적으로 발전시키고 있다 |
| 화면 이동 | (올드) Fragment + Navigation XML | Compose와 맞지 않는다 |
| | Navigation Compose 2.x | `@Serializable` 타입 경로로 개선됐지만 백스택을 라이브러리가 숨겨서 관리한다 |
| | Voyager, Decompose | 커뮤니티 KMP 라이브러리. 공식 Navigation 3가 멀티플랫폼을 지원하게 되어 필요성이 줄었다 |
| 설정 저장 | (올드) `SharedPreferences` | 동기 API라 메인 스레드를 막을 수 있고 Android 전용이다 |
| 네트워크 | Retrofit + OkHttp | Android 실무 표준이지만 JVM 전용이다. Ktorfit이 Retrofit과 비슷한 인터페이스 선언 방식을 제공한다 |
| 직렬화 | Gson, Moshi | JVM 전용이고 Gson은 Kotlin의 null 안전성을 무시한다 |
| 날짜 | (올드) `Date`, `Calendar` / `java.time` | JVM 전용이다. commonMain에서 쓸 수 없다 |
| 테스트 | Mockito, MockK | Mockito는 JVM 전용, MockK도 KMP 지원이 제한적이다. Google도 mock보다 Fake 객체를 권장한다 |

## 결과

- 모든 선택이 commonMain에서 동작해 Android·iOS가 같은 코드를 쓴다.
- 알파 단계인 Room 3.1(`kotlin.uuid.Uuid` 기본 변환), Swift export 등은 정식 출시 후 다시 검토한다.
- 라이브러리를 실제로 추가할 때는 `gradle/libs.versions.toml`에 버전을 넣고, 이 표와 다르면 새 ADR로 바꾼다.
