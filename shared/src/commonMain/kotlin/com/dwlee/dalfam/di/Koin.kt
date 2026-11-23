package com.dwlee.dalfam.di

import com.dwlee.dalfam.data.di.DataModule
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.plugin.module.dsl.startKoin

/**
 * 앱 전체 모듈 목록. DSL 시절의 `appModules` 리스트와 같은 역할이다.
 *
 * `includes`에 적은 모듈의 정의가 전부 이 모듈에 포함된다.
 * 플랫폼별 모듈(`DataPlatformModule`)은 여기 없다. 공통 코드에서는 플랫폼마다 다른 클래스를
 * 이름으로 부를 수 없어서, 그 모듈에 `@Configuration`을 붙여 자동으로 찾게 했다.
 */
@Module(includes = [DataModule::class, DomainModule::class, PresentationModule::class])
class AppModule

/**
 * Koin 시작점 표시.
 *
 * - `modules`: 명시적으로 넣을 모듈.
 * - 그리고 클래스패스에 있는 `@Configuration` 모듈(기본 라벨 "default")을 자동으로 함께 넣는다.
 *
 * 컴파일러 플러그인은 `startKoin<DalfamKoinApp>()`를 보고 여기서부터 그래프 전체를 따라가며
 * "필요한 것이 전부 등록되어 있는지"를 **빌드할 때** 검사한다(compile safety).
 * 빠진 것이 있으면 앱을 실행하기 전에 빌드가 실패한다. Hilt가 컴파일 때 그래프를 검사하는 것과 같다.
 */
@KoinApplication(modules = [AppModule::class])
object DalfamKoinApp

/**
 * Koin을 시작한다. 앱이 켜질 때 **한 번만** 호출한다.
 *
 * @param appDeclaration 플랫폼별 추가 설정. Android는 여기서 `androidContext(...)`를 넘긴다.
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin<DalfamKoinApp> {
        appDeclaration()
    }
}
