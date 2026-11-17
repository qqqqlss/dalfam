package com.dwlee.dalfam.domain.cycle.usecase

import com.dwlee.dalfam.domain.cycle.date
import com.dwlee.dalfam.domain.cycle.model.CycleAlert
import com.dwlee.dalfam.domain.cycle.model.CyclePhase
import com.dwlee.dalfam.domain.cycle.model.Fertility
import com.dwlee.dalfam.domain.cycle.model.Period
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 기본 28일 주기, 1/1 시작 기준:
 * - 다음 예정일 1/29, 배란 예정일 1/15, 가임기 1/10 ~ 1/16
 */
class GetCycleStatusUseCaseTest {
    private val getStatus = GetCycleStatusUseCase()
    private val periods = listOf(Period(startDate = date("2026-01-01"), endDate = date("2026-01-05")))

    @Test
    fun 기록이_없으면_null() {
        assertNull(getStatus(emptyList(), date("2026-01-10")))
    }

    @Test
    fun 예측_날짜() {
        val prediction = getStatus(periods, date("2026-01-01"))!!.prediction

        assertEquals(date("2026-01-29"), prediction.nextPeriodStart)
        assertEquals(date("2026-01-15"), prediction.ovulationDate)
        assertEquals(date("2026-01-10")..date("2026-01-16"), prediction.fertileWindow)
    }

    @Test
    fun 생리_기간() {
        val status = getStatus(periods, date("2026-01-03"))!!

        assertEquals(3, status.cycleDay)
        assertEquals(CyclePhase.MENSTRUAL, status.phase)
        assertEquals(Fertility.LOW, status.fertility)
        assertEquals(26, status.daysUntilNextPeriod)
        assertEquals(7, status.daysUntilFertileWindow)
    }

    @Test
    fun 종료_기록이_없으면_평균_기간으로_생리_중을_판단한다() {
        val ongoing = listOf(Period(startDate = date("2026-01-01")))

        assertEquals(CyclePhase.MENSTRUAL, getStatus(ongoing, date("2026-01-05"))!!.phase) // 5일차
        assertEquals(CyclePhase.FOLLICULAR, getStatus(ongoing, date("2026-01-06"))!!.phase) // 6일차
    }

    @Test
    fun 난포기_가임기_시작() {
        val status = getStatus(periods, date("2026-01-10"))!!

        assertEquals(CyclePhase.FOLLICULAR, status.phase)
        assertEquals(Fertility.HIGH, status.fertility)
        assertNull(status.daysUntilFertileWindow)
    }

    @Test
    fun 배란_예정일() {
        val status = getStatus(periods, date("2026-01-15"))!!

        assertEquals(CyclePhase.OVULATION, status.phase)
        assertEquals(Fertility.PEAK, status.fertility)
    }

    @Test
    fun 배란기는_예정일_전후_1일() {
        assertEquals(CyclePhase.OVULATION, getStatus(periods, date("2026-01-14"))!!.phase)
        assertEquals(CyclePhase.OVULATION, getStatus(periods, date("2026-01-16"))!!.phase)
        assertEquals(CyclePhase.LUTEAL, getStatus(periods, date("2026-01-17"))!!.phase)
    }

    @Test
    fun 예정일이_지나면_지연_일수() {
        val status = getStatus(periods, date("2026-02-01"))!!

        assertEquals(32, status.cycleDay)
        assertEquals(0, status.daysUntilNextPeriod)
        assertEquals(3, status.daysLate)
        assertTrue(status.isLate)
    }

    @Test
    fun 미래_기록은_현재_주기로_보지_않는다() {
        val withFuture = periods + Period(startDate = date("2026-01-29"))
        val status = getStatus(withFuture, date("2026-01-20"))!!

        assertEquals(date("2026-01-01"), status.prediction.currentCycleStart)
    }

    @Test
    fun 평균_주기가_35일을_넘으면_경고() {
        val long = listOf(
            Period(startDate = date("2026-01-01")),
            Period(startDate = date("2026-02-10")), // 40일
            Period(startDate = date("2026-03-22")), // 40일
        )
        assertEquals(setOf(CycleAlert.LONG_CYCLE), getStatus(long, date("2026-03-25"))!!.alerts)
    }

    @Test
    fun 주기가_하나뿐이면_길이_경고를_하지_않는다() {
        val one = listOf(
            Period(startDate = date("2026-01-01")),
            Period(startDate = date("2026-02-10")), // 40일
        )
        assertTrue(getStatus(one, date("2026-02-12"))!!.alerts.isEmpty())
    }
}
