package com.dwlee.dalfam.domain.usecase

import com.dwlee.dalfam.domain.model.Period
import com.dwlee.dalfam.domain.repository.PeriodRepository
import com.dwlee.dalfam.domain.time.DateProvider
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus

sealed interface RecordPeriodStartResult {
    data class Success(val id: Long) : RecordPeriodStartResult

    /** 미래 날짜는 기록할 수 없다. */
    data object FutureDate : RecordPeriodStartResult

    /** 진행 중인 생리가 이미 있다 (시작한 지 얼마 안 됨). */
    data object AlreadyOngoing : RecordPeriodStartResult

    /** 다른 기록의 기간과 겹친다. */
    data object Overlaps : RecordPeriodStartResult
}

/**
 * "시작했어요" 원탭 기록.
 *
 * 자동화 규칙:
 * - 날짜를 안 주면 오늘로 기록한다.
 * - 이전 생리가 종료 기록 없이 진행 중이면 평균 기간으로 자동 마감한다.
 * - 과거 날짜를 뒤늦게 기록하면 종료일도 평균 기간으로 채운다
 *   (가장 최근 기록만 진행 중 상태가 될 수 있다).
 */
class RecordPeriodStartUseCase(
    private val repository: PeriodRepository,
    private val dateProvider: DateProvider,
    private val calculateStats: CalculateCycleStatsUseCase = CalculateCycleStatsUseCase(),
) {

    suspend operator fun invoke(date: LocalDate = dateProvider.today()): RecordPeriodStartResult {
        if (date > dateProvider.today()) return RecordPeriodStartResult.FutureDate

        val periods = repository.getPeriods()
        val averagePeriodLength = calculateStats(periods).averagePeriodLength

        if (periods.any { it.startDate == date || it.endDate?.let { end -> date in it.startDate..end } == true }) {
            return RecordPeriodStartResult.Overlaps
        }

        val previous = periods.filter { it.startDate < date }.maxByOrNull { it.startDate }
        val next = periods.filter { it.startDate > date }.minByOrNull { it.startDate }

        if (previous != null && previous.isOngoing) {
            if (previous.startDate.daysUntil(date) < MIN_DAYS_BETWEEN_STARTS) {
                return RecordPeriodStartResult.AlreadyOngoing
            }
            val estimatedEnd = minOf(
                previous.startDate.plus(averagePeriodLength - 1, DateTimeUnit.DAY),
                date.minus(1, DateTimeUnit.DAY),
            )
            repository.update(previous.copy(endDate = estimatedEnd))
        }

        val endDate = next?.let {
            minOf(
                date.plus(averagePeriodLength - 1, DateTimeUnit.DAY),
                it.startDate.minus(1, DateTimeUnit.DAY),
            )
        }
        val id = repository.insert(Period(startDate = date, endDate = endDate))
        return RecordPeriodStartResult.Success(id)
    }

    companion object {
        /** 이 기간 안에 다시 "시작"을 누르면 실수로 보고 막는다. */
        const val MIN_DAYS_BETWEEN_STARTS = 15
    }
}
