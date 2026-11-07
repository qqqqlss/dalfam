# 달팸 (dalfam)

생리 주기를 기록하고 다음 생리 예정일을 알려주는 앱입니다.

> 생리 기록은 민감한 건강 정보이므로, 현재는 기기 안에만 저장하고 외부로 전송하지 않습니다.

## 기술 스택

- **언어**: Kotlin (공통 코드는 순수 Kotlin, `commonMain`에서 `java.*` 미사용)
- **멀티플랫폼**: Kotlin Multiplatform + Compose Multiplatform (Android, iOS UI 공유)
- **날짜**: kotlinx-datetime
- **아키텍처**: 클린 아키텍처(계층별 모듈, UseCase), 단방향 데이터 흐름(UDF), 코루틴·Flow
- **DI**: Koin (+ Compiler Plugin)
- **로컬 DB**: Room 3 (KMP)
- **화면 이동**: Navigation 3
- **설정 저장**: DataStore
- **네트워크**: Ktor Client (+ 필요 시 Ktorfit)
- **직렬화**: kotlinx-serialization
- **서버 (예정)**: Kotlin + Spring Boot, AWS 서울 리전

## 모듈 구조

| 경로 | 내용 |
|---|---|
| `androidApp/` | Android 앱 진입점 |
| `iosApp/` | iOS 앱 진입점 (Xcode 프로젝트) |
| `shared/` | Compose UI, ViewModel, DI 조립 (Android·iOS 공유) |
| `domain/` | 도메인 모델, UseCase — 순수 Kotlin (예정) |
| `data/` | Room·DataStore·네트워크, Repository 구현 (예정) |
| `server/` | Spring Boot 서버 (예정) |

라이브러리·Kotlin 버전은 [`gradle/libs.versions.toml`](gradle/libs.versions.toml) 한 곳에서 관리합니다.

## 빌드

- Android 디버그 빌드: `./gradlew :androidApp:assembleDebug`
- 공통 테스트(Android 호스트): `./gradlew :shared:testAndroidHostTest`
- iOS: macOS에서 [`iosApp`](iosApp)을 Xcode로 열어 실행

비밀값(서명 키, API 키 등)은 저장소에 커밋하지 않고 `local.properties`나 환경변수로 주입합니다.

## 문서

- [`CLAUDE.md`](CLAUDE.md) — 프로젝트 규칙
- [`docs/adr/`](docs/adr) — 아키텍처 결정 기록
- [`docs/engineering-checklist.md`](docs/engineering-checklist.md) — 단계별로 갖춰 나갈 규칙·도구 목록

## 라이선스

Copyright (c) 2026 dwlee. All rights reserved.

이 저장소의 코드는 공개되어 있지만 오픈소스 라이선스로 배포되지 않습니다. 저작권자의 허락 없이 복제·수정·배포할 수 없습니다.
