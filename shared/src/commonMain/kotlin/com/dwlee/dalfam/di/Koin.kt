package com.dwlee.dalfam.di

import com.dwlee.dalfam.data.di.dataModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

/** 플랫폼별 등록 (DB 빌더 등). androidMain / iosMain에서 actual로 채운다. */
internal expect val platformModule: Module

/** 앱 전체 모듈 목록. 검증 테스트에서도 같은 목록을 쓴다. */
val appModules: List<Module>
    get() = listOf(platformModule, dataModule, domainModule, presentationModule)

/**
 * Koin을 시작한다. 앱이 켜질 때 **한 번만** 호출한다.
 *
 * @param appDeclaration 플랫폼별 추가 설정. Android는 여기서 `androidContext(...)`를 넘긴다.
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(appModules)
    }
}
