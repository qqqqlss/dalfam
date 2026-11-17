package com.dwlee.dalfam.domain.cycle.usecase

import com.dwlee.dalfam.domain.cycle.model.CycleAlert
import com.dwlee.dalfam.domain.cycle.model.CyclePhase
import com.dwlee.dalfam.domain.cycle.model.CyclePrediction
import com.dwlee.dalfam.domain.cycle.model.CycleStats
import com.dwlee.dalfam.domain.cycle.model.CycleStatus
import com.dwlee.dalfam.domain.cycle.model.Fertility
import com.dwlee.dalfam.domain.cycle.model.Period
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlin.math.abs

/**
 * 기록과 오늘 날짜로 "오늘의 상태"를 계산한다. 순수 함수라 UI·DB 없이 테스트할 수 있다.
 *
 * @return 오늘 이전에 시작한 기록이 하나도 없으면 null.
 */
class GetCycleStatusUseCase(
    private val calculateStats: CalculateCycleStatsUseCase = CalculateCycleStatsUseCase(),
    private val predictCycle: PredictCycleUseCase = PredictCycleUseCase(),
) {

    operator fun invoke(periods: List<Period>, today: LocalDate): CycleStatus? {
        val current = periods
            .filter { it.startDate <= today }
            .maxByOrNull { it.startDate }
            ?: return null

        val stats = calculateStats(periods)
        val prediction = predictCycle(current.startDate, stats)

        return CycleStatus(
            today = today,
            cycleDay = current.startDate.daysUntil(today) + 1,
            phase = phaseOf(current, today, stats, prediction),
            fertility = fertilityOf(today, prediction),
            daysUntilNextPeriod = today.daysUntil(prediction.nextPeriodStart).coerceAtLeast(0),
            daysLate = prediction.nextPeriodStart.daysUntil(today).coerceAtLeast(0),
            daysUntilFertileWindow = prediction.fertileWindow.start
                .takeIf { today < it }
                ?.let { today.daysUntil(it) },
            prediction = prediction,
            stats = stats,
            alerts = alertsOf(stats),
        )
    }

    private fun phaseOf(
        current: Period,
        today: LocalDate,
        stats: CycleStats,
        prediction: CyclePrediction,
    ): CyclePhase {
        val isMenstrual = current.endDate
            ?.let { today <= it }
            ?: (current.startDate.daysUntil(today) < stats.averagePeriodLength)
        val daysFromOvulation = prediction.ovulationDate.daysUntil(today)

        return when {
            isMenstrual -> CyclePhase.MENSTRUAL
            abs(daysFromOvulation) <= OVULATION_PHASE_RANGE -> CyclePhase.OVULATION
            daysFromOvulation < 0 -> CyclePhase.FOLLICULAR
            else -> CyclePhase.LUTEAL
        }
    }

    private fun fertilityOf(today: LocalDate, prediction: CyclePrediction): Fertility = when {
        today == prediction.ovulationDate -> Fertility.PEAK
        today in prediction.fertileWindow -> Fertility.HIGH
        else -> Fertility.LOW
    }

    private fun alertsOf(stats: CycleStats): Set<CycleAlert> = buildSet {
        if (stats.sampleSize >= MIN_SAMPLES_FOR_ALERT) {
            if (stats.averageCycleLength < SHORT_CYCLE_THRESHOLD) add(CycleAlert.SHORT_CYCLE)
            if (stats.averageCycleLength > LONG_CYCLE_THRESHOLD) add(CycleAlert.LONG_CYCLE)
        }
        if (stats.isIrregular) add(CycleAlert.IRREGULAR)
    }

    companion object {
        const val OVULATION_PHASE_RANGE = 1
        const val MIN_SAMPLES_FOR_ALERT = 2
        const val SHORT_CYCLE_THRESHOLD = 21
        const val LONG_CYCLE_THRESHOLD = 35
    }
}
