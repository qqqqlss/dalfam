package com.dwlee.dalfam.domain.usecase

import com.dwlee.dalfam.domain.model.CycleStatus
import com.dwlee.dalfam.domain.repository.PeriodRepository
import com.dwlee.dalfam.domain.time.DateProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 기록이 바뀔 때마다 오늘의 상태를 다시 계산해 흘려보낸다.
 *
 * 날짜가 바뀌는 것(자정)은 감지하지 않으므로, 화면이 다시 보일 때 다시 구독한다.
 */
class ObserveCycleStatusUseCase(
    private val repository: PeriodRepository,
    private val dateProvider: DateProvider,
    private val getCycleStatus: GetCycleStatusUseCase = GetCycleStatusUseCase(),
) {
    operator fun invoke(): Flow<CycleStatus?> =
        repository.observePeriods().map { periods -> getCycleStatus(periods, dateProvider.today()) }
}
