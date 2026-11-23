package com.dwlee.dalfam.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwlee.dalfam.domain.cycle.usecase.ObserveCycleStatusUseCase
import com.dwlee.dalfam.domain.cycle.usecase.RecordPeriodEndResult
import com.dwlee.dalfam.domain.cycle.usecase.RecordPeriodEndUseCase
import com.dwlee.dalfam.domain.cycle.usecase.RecordPeriodStartResult
import com.dwlee.dalfam.domain.cycle.usecase.RecordPeriodStartUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 홈 화면의 상태를 들고 있고, 버튼 이벤트를 UseCase로 넘긴다.
 *
 * 단방향 데이터 흐름(UDF):
 * ```
 * 화면 --(onStartClick 같은 이벤트)--> ViewModel --> UseCase --> DB
 *   ^                                                         |
 *   +------------------(uiState: StateFlow)-------------------+
 * ```
 * 화면은 상태를 직접 바꾸지 않는다. 이벤트만 보내고, 바뀐 상태를 받아서 다시 그린다.
 */
class HomeViewModel(
    observeCycleStatus: ObserveCycleStatusUseCase,
    private val recordPeriodStart: RecordPeriodStartUseCase,
    private val recordPeriodEnd: RecordPeriodEndUseCase,
) : ViewModel() {

    // 바깥에는 읽기 전용(StateFlow)만 보이고, 바꾸는 건 ViewModel 안에서만 한다.
    private val message = MutableStateFlow<HomeMessage?>(null)

    /**
     * DB 기록(Flow)과 안내 메시지를 합쳐 화면 상태 하나로 만든다.
     *
     * - `stateIn`: 차가운 Flow를 "항상 최신 값을 하나 들고 있는" StateFlow로 바꾼다.
     * - `WhileSubscribed(5_000)`: 화면이 안 보이면 5초 뒤 DB 구독을 멈춘다.
     *   화면 회전처럼 잠깐 끊기는 경우에는 다시 구독하지 않도록 5초 여유를 둔다.
     */
    val uiState: StateFlow<HomeUiState> =
        combine(observeCycleStatus(), message) { status, message ->
            HomeUiState(isLoading = false, status = status, message = message)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )

    fun onStartClick() {
        // viewModelScope: 화면이 완전히 사라지면(ViewModel이 버려지면) 자동으로 취소된다.
        viewModelScope.launch {
            message.value = when (recordPeriodStart()) {
                is RecordPeriodStartResult.Success -> HomeMessage.PERIOD_STARTED
                RecordPeriodStartResult.AlreadyOngoing -> HomeMessage.ALREADY_ONGOING
                RecordPeriodStartResult.Overlaps -> HomeMessage.OVERLAPS
                RecordPeriodStartResult.FutureDate -> HomeMessage.FUTURE_DATE
            }
        }
    }

    fun onEndClick() {
        viewModelScope.launch {
            message.value = when (recordPeriodEnd()) {
                RecordPeriodEndResult.Success -> HomeMessage.PERIOD_ENDED
                RecordPeriodEndResult.NoOngoingPeriod -> HomeMessage.NO_ONGOING_PERIOD
                RecordPeriodEndResult.FutureDate -> HomeMessage.FUTURE_DATE
                RecordPeriodEndResult.BeforeStart -> HomeMessage.BEFORE_START
            }
        }
    }

    /** 스낵바를 보여 준 뒤 호출. 지우지 않으면 화면 회전 때 같은 안내가 또 뜬다. */
    fun onMessageShown() {
        message.value = null
    }
}
