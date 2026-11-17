package com.dwlee.dalfam.domain.cycle.usecase

import com.dwlee.dalfam.domain.cycle.PeriodRepository
import com.dwlee.dalfam.domain.common.time.DateProvider
import kotlinx.datetime.LocalDate

sealed interface RecordPeriodEndResult {
    data object Success : RecordPeriodEndResult

    /** 진행 중인 생리가 없다. */
    data object NoOngoingPeriod : RecordPeriodEndResult

    data object FutureDate : RecordPeriodEndResult

    /** 종료일이 시작일보다 빠르다. */
    data object BeforeStart : RecordPeriodEndResult
}

/** "끝났어요" 기록. 진행 중인 가장 최근 생리를 마감한다. */
class RecordPeriodEndUseCase(
    private val repository: PeriodRepository,
    private val dateProvider: DateProvider,
) {

    suspend operator fun invoke(date: LocalDate = dateProvider.today()): RecordPeriodEndResult {
        if (date > dateProvider.today()) return RecordPeriodEndResult.FutureDate

        val latest = repository.getPeriods().maxByOrNull { it.startDate }
        if (latest == null || !latest.isOngoing) return RecordPeriodEndResult.NoOngoingPeriod
        if (date < latest.startDate) return RecordPeriodEndResult.BeforeStart

        repository.update(latest.copy(endDate = date))
        return RecordPeriodEndResult.Success
    }
}
