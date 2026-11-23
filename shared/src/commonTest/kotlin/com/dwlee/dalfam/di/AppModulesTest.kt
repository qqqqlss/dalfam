package com.dwlee.dalfam.di

import com.dwlee.dalfam.InMemoryPeriodRepository
import com.dwlee.dalfam.domain.repository.PeriodRepository
import com.dwlee.dalfam.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * [domainModule]·[presentationModule]에 등록된 것을 **실제로 꺼내 봐서** 등록 누락을 잡는다.
 *
 * Koin의 `verify()`는 생성자 시그니처만 보기 때문에, 기본값이 있는 파라미터를
 * DSL 람다 안에서 `get()`으로 넘기는 경우의 누락은 잡지 못한다. 그래서 직접 만들어 본다.
 * (data 쪽 조립은 data 모듈의 DataModuleTest가 실제 DB로 검증한다.)
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
        // PeriodRepository는 data 모듈 몫이라 여기서는 메모리 저장소로 채운다.
        val koin = koinApplication {
            modules(
                domainModule,
                presentationModule,
                module { single<PeriodRepository> { InMemoryPeriodRepository() } },
            )
        }.koin

        // ViewModel → UseCase → Repository·DateProvider까지 줄줄이 만들어진다.
        koin.get<HomeViewModel>()

        koin.close()
    }
}
