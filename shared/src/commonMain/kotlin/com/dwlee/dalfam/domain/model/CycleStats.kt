package com.dwlee.dalfam.domain.model

/**
 * 기록으로부터 계산한 주기 통계.
 *
 * @property sampleSize 평균 계산에 쓰인 주기 수. 0이면 기본값으로 추정한 상태.
 * @property isIrregular 최근 주기 길이의 편차가 커서 예측 정확도가 낮은 상태.
 */
data class CycleStats(
    val averageCycleLength: Int,
    val averagePeriodLength: Int,
    val sampleSize: Int,
    val isIrregular: Boolean,
) {
    val isDefault: Boolean get() = sampleSize == 0

    companion object {
        const val DEFAULT_CYCLE_LENGTH = 28
        const val DEFAULT_PERIOD_LENGTH = 5

        val Default = CycleStats(
            averageCycleLength = DEFAULT_CYCLE_LENGTH,
            averagePeriodLength = DEFAULT_PERIOD_LENGTH,
            sampleSize = 0,
            isIrregular = false,
        )
    }
}
