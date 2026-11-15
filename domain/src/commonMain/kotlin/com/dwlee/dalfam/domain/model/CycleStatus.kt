package com.dwlee.dalfam.domain.model

import kotlinx.datetime.LocalDate

/** 주기 단계. */
enum class CyclePhase {
    /** 생리 기간 */
    MENSTRUAL,

    /** 생리 이후 ~ 배란 전 */
    FOLLICULAR,

    /** 배란 예정일 전후 1일 */
    OVULATION,

    /** 배란 이후 ~ 다음 생리 전 */
    LUTEAL,
}

/** 임신 가능성 단계 (추정치). */
enum class Fertility {
    LOW,

    /** 가임기 */
    HIGH,

    /** 배란 예정일 */
    PEAK,
}

/** 병원 상담을 권할 만한 신호. AI가 아니라 도메인 규칙으로만 판단한다. */
enum class CycleAlert {
    /** 평균 주기 21일 미만 */
    SHORT_CYCLE,

    /** 평균 주기 35일 초과 */
    LONG_CYCLE,

    /** 최근 주기 길이 편차가 큼 */
    IRREGULAR,
}

/**
 * 홈 화면 상태 카드에 보여줄 "오늘"의 상태.
 *
 * @property cycleDay 현재 주기의 몇 번째 날인지 (시작일 = 1일차).
 * @property daysUntilNextPeriod 다음 예정일까지 남은 일수. 예정일이 지났으면 0.
 * @property daysLate 예정일이 지난 일수. 지나지 않았으면 0.
 * @property daysUntilFertileWindow 가임기 시작까지 남은 일수. 가임기 중이거나 지났으면 null.
 */
data class CycleStatus(
    val today: LocalDate,
    val cycleDay: Int,
    val phase: CyclePhase,
    val fertility: Fertility,
    val daysUntilNextPeriod: Int,
    val daysLate: Int,
    val daysUntilFertileWindow: Int?,
    val prediction: CyclePrediction,
    val stats: CycleStats,
    val alerts: Set<CycleAlert>,
) {
    val isLate: Boolean get() = daysLate > 0
}
