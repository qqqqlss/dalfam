package com.dwlee.dalfam.domain.usecase

import com.dwlee.dalfam.domain.date
import com.dwlee.dalfam.domain.model.CycleStats
import com.dwlee.dalfam.domain.model.Period
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CalculateCycleStatsUseCaseTest {
    private val calculate = CalculateCycleStatsUseCase()

    @Test
    fun 기록이_없으면_기본값() {
        assertEquals(CycleStats.Default, calculate(emptyList()))
    }

    @Test
    fun 기록이_하나면_주기는_기본값_기간은_기록값() {
        val stats = calculate(listOf(Period(startDate = date("2026-01-01"), endDate = date("2026-01-04"))))

        assertEquals(28, stats.averageCycleLength)
        assertEquals(4, stats.averagePeriodLength)
        assertEquals(0, stats.sampleSize)
    }

    @Test
    fun 시작일_간격으로_평균_주기를_계산한다() {
        val stats = calculate(
            listOf(
                Period(startDate = date("2026-03-01")), // 순서가 섞여도 정렬해서 계산
                Period(startDate = date("2026-01-01")), // → 30일
                Period(startDate = date("2026-01-31")), // → 29일
            ),
        )

        assertEquals(30, stats.averageCycleLength) // (30 + 29) / 2 = 29.5 → 30
        assertEquals(2, stats.sampleSize)
    }

    @Test
    fun 기록_누락으로_보이는_긴_간격은_제외한다() {
        val stats = calculate(
            listOf(
                Period(startDate = date("2026-01-01")),
                Period(startDate = date("2026-01-29")), // 28일
                Period(startDate = date("2026-05-01")), // 92일 → 제외
            ),
        )

        assertEquals(28, stats.averageCycleLength)
        assertEquals(1, stats.sampleSize)
    }

    @Test
    fun 최근_6개_주기만_사용한다() {
        // 첫 주기만 40일, 이후 6개 주기는 28일
        val starts = mutableListOf(date("2025-01-01"), date("2025-02-10"))
        repeat(6) { starts += starts.last().plus28() }
        val stats = calculate(starts.map { Period(startDate = it) })

        assertEquals(28, stats.averageCycleLength)
        assertEquals(6, stats.sampleSize)
    }

    @Test
    fun 편차가_7일을_넘으면_불규칙() {
        val stats = calculate(
            listOf(
                Period(startDate = date("2026-01-01")),
                Period(startDate = date("2026-01-25")), // 24일
                Period(startDate = date("2026-03-03")), // 37일
                Period(startDate = date("2026-03-31")), // 28일
            ),
        )
        assertTrue(stats.isIrregular)
    }

    @Test
    fun 주기가_3개_미만이면_불규칙_판단을_하지_않는다() {
        val stats = calculate(
            listOf(
                Period(startDate = date("2026-01-01")),
                Period(startDate = date("2026-01-22")), // 21일
                Period(startDate = date("2026-03-01")), // 38일
            ),
        )
        assertFalse(stats.isIrregular)
    }

    private fun LocalDate.plus28() = plus(28, DateTimeUnit.DAY)
}
