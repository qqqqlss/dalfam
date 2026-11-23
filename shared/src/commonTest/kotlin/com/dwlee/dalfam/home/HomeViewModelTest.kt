package com.dwlee.dalfam.home

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.dwlee.dalfam.InMemoryPeriodRepository
import com.dwlee.dalfam.domain.model.CyclePhase
import com.dwlee.dalfam.domain.time.DateProvider
import com.dwlee.dalfam.domain.usecase.ObserveCycleStatusUseCase
import com.dwlee.dalfam.domain.usecase.RecordPeriodEndUseCase
import com.dwlee.dalfam.domain.usecase.RecordPeriodStartUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * ViewModel을 Koin 없이 직접 만들어서 검증한다(생성자 주입이라 가능).
 *
 * viewModelScope는 Dispatchers.Main에서 돈다. 테스트에는 진짜 Main 스레드가 없으므로
 * 테스트용 Dispatcher로 바꿔 끼운다(setMain).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val today = LocalDate.parse("2026-03-10")
    private val dateProvider = DateProvider { today }
    private val repository = InMemoryPeriodRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = HomeViewModel(
        observeCycleStatus = ObserveCycleStatusUseCase(repository, dateProvider),
        recordPeriodStart = RecordPeriodStartUseCase(repository, dateProvider),
        recordPeriodEnd = RecordPeriodEndUseCase(repository, dateProvider),
    )

    @Test
    fun 처음에는_로딩_상태다() {
        assertEquals(HomeUiState(isLoading = true), createViewModel().uiState.value)
    }

    @Test
    fun 기록이_없으면_로딩이_끝나고_상태는_비어있다() = runTest {
        createViewModel().uiState.test {
            val state = awaitUntil { !it.isLoading }
            assertNull(state.status)
            assertNull(state.message)
        }
    }

    @Test
    fun 시작했어요를_누르면_오늘이_1일차가_되고_안내가_뜬다() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.test {
            awaitUntil { !it.isLoading }

            viewModel.onStartClick()

            val state = awaitUntil { it.status != null && it.message != null }
            assertEquals(1, state.status?.cycleDay)
            assertEquals(CyclePhase.MENSTRUAL, state.status?.phase)
            assertEquals(HomeMessage.PERIOD_STARTED, state.message)
        }
    }

    @Test
    fun 안내를_보여준_뒤에는_지워진다() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.test {
            viewModel.onStartClick()
            awaitUntil { it.message != null }

            viewModel.onMessageShown()

            awaitUntil { it.message == null }
        }
    }

    @Test
    fun 진행_중인_생리가_없을_때_끝났어요를_누르면_안내만_뜬다() = runTest {
        val viewModel = createViewModel()
        viewModel.uiState.test {
            awaitUntil { !it.isLoading }

            viewModel.onEndClick()

            val state = awaitUntil { it.message != null }
            assertEquals(HomeMessage.NO_ONGOING_PERIOD, state.message)
            assertNull(state.status)
        }
    }

    /** 조건을 만족하는 상태가 올 때까지 기다린다. 중간 상태(로딩 등)는 건너뛴다. */
    private suspend fun ReceiveTurbine<HomeUiState>.awaitUntil(
        predicate: (HomeUiState) -> Boolean,
    ): HomeUiState {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }
}
