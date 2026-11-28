package com.dwlee.dalfam.di

import com.dwlee.dalfam.InMemoryPeriodRepository
import com.dwlee.dalfam.domain.cycle.PeriodRepository
import com.dwlee.dalfam.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import org.koin.plugin.module.dsl.koinApplication
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/** PeriodRepository는 data 모듈 몫이라, 테스트에서는 메모리 저장소로 채운다. */
@Module
class InMemoryRepositoryModule {

    @Singleton
    fun periodRepository(): PeriodRepository = InMemoryPeriodRepository()
}

/** 테스트용 시작점. data 모듈(`DataModule`) 대신 메모리 저장소를 쓴다. */
@KoinApplication(modules = [DomainModule::class, PresentationModule::class, InMemoryRepositoryModule::class])
object AppModulesTestApp

/**
 * [DomainModule]·[PresentationModule]에 등록된 것을 **실제로 꺼내 봐서** 등록이 동작하는지 확인한다.
 *
 * 등록 누락 자체는 이제 컴파일러 플러그인이 빌드할 때 잡는다(compile safety).
 * 이 테스트에서 [HomeViewModel]이 빠져 있었다면 `koin.get<HomeViewModel>()` 줄에서 컴파일이 실패한다.
 * 그래도 실제로 만들어 보는 테스트는 남겨 둔다. 생성 중 예외(예: 잘못된 초기화)는 실행해야 알 수 있다.
 * (data 쪽 조립은 data 모듈의 DataModuleTest가 실제 DB로 검증한다.)
 *
 * 왜 commonTest가 아니라 androidHostTest인가?
 * Koin Compiler Plugin 1.2.1은 **iOS 테스트 컴파일**에서 `@ComponentScan`으로 찾은 클래스
 * (`@KoinViewModel class HomeViewModel`)를 보지 못해 "정의가 없다"며 컴파일을 막는다
 * (2026-11-23, Kotlin 2.4.21). 앱 코드는 문제없다. iOS 메인 컴파일의 `startKoin` 검사는 통과한다.
 * 플러그인이 고쳐지면 commonTest로 되돌린다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppModulesTest {

    @BeforeTest
    fun setUp() {
        // ViewModel이 만들어질 때 viewModelScope(Main)를 쓰므로 테스트용 Main을 끼운다.
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun 화면에서_쓰는_ViewModel을_만들_수_있다() {
        val koin = koinApplication<AppModulesTestApp>().koin

        // ViewModel → UseCase → Repository·DateProvider까지 줄줄이 만들어진다.
        koin.get<HomeViewModel>()

        koin.close()
    }
}
