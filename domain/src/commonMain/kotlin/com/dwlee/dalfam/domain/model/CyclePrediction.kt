package com.dwlee.dalfam.domain.model

import kotlinx.datetime.LocalDate

/**
 * 현재 주기에 대한 예측. 모든 날짜는 달력 계산법 기반 추정치다.
 *
 * - 배란 예정일 = 다음 생리 예정일 − 14일
 * - 가임기 = 배란 예정일 − 5일 ~ + 1일
 */
data class CyclePrediction(
    val currentCycleStart: LocalDate,
    val nextPeriodStart: LocalDate,
    val ovulationDate: LocalDate,
    val fertileWindow: ClosedRange<LocalDate>,
)
