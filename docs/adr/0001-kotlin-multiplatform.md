# 0001. Kotlin Multiplatform + Compose Multiplatform, Android 우선

- 상태: 채택
- 날짜: 2026-11-07

## 배경

Android와 iOS에 같은 앱을 내고 싶지만, 개발 PC는 Windows라 당장 iOS 빌드를 할 수 없다. 개인 공부 목적도 있어 Kotlin을 깊게 익히고 싶다.

## 결정

- Kotlin Multiplatform + Compose Multiplatform으로 Android·iOS가 UI까지 공유한다 (Share UI).
- 당장은 Android만 빌드하고 iOS는 Mac 확보 후 진행한다. Desktop·Web 타깃은 만들지 않는다.
- minSdk 26. 예정일 알림에 쓰는 NotificationChannel(API 26+)의 버전 분기를 없애기 위해서다.
- 공통 코드는 순수 Kotlin으로 쓴다(`commonMain`에서 `java.*` 금지).
- 주요 라이브러리: kotlinx-datetime, Koin, Room(KMP), DataStore, Ktor Client, kotlinx-serialization.

## 고려한 대안

- Android 네이티브만 — iOS를 낼 때 처음부터 다시 만들어야 한다.
- Flutter / React Native — Kotlin 학습이라는 목적과 맞지 않는다.
- KMP + 플랫폼별 네이티브 UI(SwiftUI) — Mac 없이 iOS UI를 만들 수 없고, 혼자 UI를 두 벌 관리하기 부담스럽다.

## 결과

- Windows에서도 대부분의 코드(UI 포함)를 작성·테스트할 수 있다.
- iOS 전용 문제는 Mac을 확보할 때까지 발견되지 않는다. `commonMain`을 순수 Kotlin으로 유지해 이 위험을 줄인다.
- minSdk 26 미만 기기(2026년 기준 극소수)는 지원하지 않는다.
