package com.dwlee.dalfam.domain.usecase

import com.dwlee.dalfam.domain.model.CycleStats
import com.dwlee.dalfam.domain.model.Period
import kotlinx.datetime.daysUntil
import kotlin.math.roundToInt

/**
 * 기록으로부터 평균 주기·기간을 계산한다.
 *
 * - 최근 [RECENT_COUNT]개 주기만 사용해 최근 경향을 반영한다.
 * - [VALID_CYCLE_LENGTH] 밖의 주기는 기록 누락으로 보고 평균에서 제외한다.
 * - 기록이 부족하면 [CycleStats.Default] 값을 쓴다.
 */
class CalculateCycleStatsUseCase {

    operator fun invoke(periods: List<Period>): CycleStats {
        val sorted = periods.sortedBy { it.startDate }

        val cycleLengths = sorted
            .zipWithNext { prev, next -> prev.startDate.daysUntil(next.startDate) }
            .filter { it in VALID_CYCLE_LENGTH }
            .takeLast(RECENT_COUNT)

        val periodLengths = sorted
            .mapNotNull { p -> p.endDate?.let { p.startDate.daysUntil(it) + 1 } }
            .filter { it in VALID_PERIOD_LENGTH }
            .takeLast(RECENT_COUNT)

        return CycleStats(
            averageCycleLength = cycleLengths.averageOrNull() ?: CycleStats.DEFAULT_CYCLE_LENGTH,
            averagePeriodLength = periodLengths.averageOrNull() ?: CycleStats.DEFAULT_PERIOD_LENGTH,
            sampleSize = cycleLengths.size,
            isIrregular = cycleLengths.size >= MIN_SAMPLES_FOR_IRREGULAR &&
                cycleLengths.max() - cycleLengths.min() > IRREGULAR_VARIATION_DAYS,
        )
    }

    private fun List<Int>.averageOrNull(): Int? =
        if (isEmpty()) null else average().roundToInt()

    companion object {
        const val RECENT_COUNT = 6
        val VALID_CYCLE_LENGTH = 15..60
        val VALID_PERIOD_LENGTH = 1..14
        const val MIN_SAMPLES_FOR_IRREGULAR = 3
        const val IRREGULAR_VARIATION_DAYS = 7
    }
}
