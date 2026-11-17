package com.dwlee.dalfam.domain.cycle.model

import kotlinx.datetime.LocalDate

/**
 * 한 번의 생리 기록.
 *
 * @property endDate 아직 진행 중이면 null. 사용자가 종료를 기록하지 않아도
 *   다음 생리 시작을 기록할 때 평균 기간으로 자동 마감된다.
 */
data class Period(
    val id: Long = 0,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
) {
    init {
        require(endDate == null || endDate >= startDate) { "endDate는 startDate보다 빠를 수 없습니다." }
    }

    val isOngoing: Boolean get() = endDate == null
}
