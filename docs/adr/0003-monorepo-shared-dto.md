# 0003. 모노레포와 공유 DTO

- 상태: 채택
- 날짜: 2026-11-07

## 배경

나중에 Kotlin + Spring Boot 서버를 붙일 예정이다. 앱과 서버가 같은 요청·응답 형식을 따로 정의하면 둘이 어긋나기 쉽다.

## 결정

- 저장소 하나에 `androidApp/`, `iosApp/`, `shared/`, `server/`(예정)를 둔다.
- 앱과 서버가 공유하는 DTO는 `shared`에 `@Serializable`로 정의하고, 서버도 Jackson 대신 kotlinx-serialization으로 읽는다.
- Kotlin 버전은 `gradle/libs.versions.toml` 한 곳에서 관리해 앱과 서버가 같은 버전을 쓴다.

## 고려한 대안

- 앱·서버 저장소 분리 — DTO를 패키지로 배포하거나 복사해야 한다. 혼자 개발하는 규모에서는 관리 비용이 크다.
- OpenAPI 스펙에서 코드 생성 — 언어가 둘 다 Kotlin이라 직접 공유하는 편이 단순하다.

## 결과

- DTO를 바꾸면 앱과 서버가 같은 커밋에서 함께 컴파일되어 불일치를 바로 발견한다.
- 서버가 `shared`에 의존하게 되므로, DTO는 Compose 등 UI 의존성과 섞이지 않게 둬야 한다. 서버를 붙일 때 DTO를 별도 모듈로 분리할지 다시 판단한다.
