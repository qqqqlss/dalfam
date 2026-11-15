package com.dwlee.dalfam.domain.usecase

import com.dwlee.dalfam.domain.model.CyclePrediction
import com.dwlee.dalfam.domain.model.CycleStats
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * 현재 주기 시작일과 통계로 다음 예정일·배란일·가임기를 추정한다 (달력 계산법).
 */
class PredictCycleUseCase {

    operator fun invoke(currentCycleStart: LocalDate, stats: CycleStats): CyclePrediction {
        val nextPeriodStart = currentCycleStart.plus(stats.averageCycleLength, DateTimeUnit.DAY)
        val ovulationDate = nextPeriodStart.minus(LUTEAL_PHASE_DAYS, DateTimeUnit.DAY)
        val fertileStart = ovulationDate.minus(FERTILE_DAYS_BEFORE_OVULATION, DateTimeUnit.DAY)
        val fertileEnd = ovulationDate.plus(FERTILE_DAYS_AFTER_OVULATION, DateTimeUnit.DAY)

        return CyclePrediction(
            currentCycleStart = currentCycleStart,
            nextPeriodStart = nextPeriodStart,
            ovulationDate = ovulationDate,
            fertileWindow = fertileStart..fertileEnd,
        )
    }

    companion object {
        /** 배란 후 다음 생리까지는 비교적 일정하게 약 14일. */
        const val LUTEAL_PHASE_DAYS = 14
        const val FERTILE_DAYS_BEFORE_OVULATION = 5
        const val FERTILE_DAYS_AFTER_OVULATION = 1
    }
}
