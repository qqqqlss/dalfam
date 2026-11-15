package com.dwlee.dalfam.domain.usecase

import com.dwlee.dalfam.domain.FakePeriodRepository
import com.dwlee.dalfam.domain.date
import com.dwlee.dalfam.domain.model.CyclePhase
import com.dwlee.dalfam.domain.model.Period
import com.dwlee.dalfam.domain.time.DateProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class RecordPeriodUseCaseTest {
    private val today = date("2026-02-01")
    private val dateProvider = DateProvider { today }

    private fun startUseCase(repo: FakePeriodRepository) = RecordPeriodStartUseCase(repo, dateProvider)
    private fun endUseCase(repo: FakePeriodRepository) = RecordPeriodEndUseCase(repo, dateProvider)

    @Test
    fun 날짜를_안_주면_오늘로_기록() = runTest {
        val repo = FakePeriodRepository()

        assertIs<RecordPeriodStartResult.Success>(startUseCase(repo)())
        assertEquals(listOf(Period(id = 1, startDate = today)), repo.periods)
    }

    @Test
    fun 미래_날짜는_막는다() = runTest {
        val repo = FakePeriodRepository()
        assertEquals(RecordPeriodStartResult.FutureDate, startUseCase(repo)(date("2026-02-02")))
    }

    @Test
    fun 진행_중인_생리를_종료_없이_두면_다음_시작_때_평균_기간으로_자동_마감() = runTest {
        val repo = FakePeriodRepository(listOf(Period(startDate = date("2026-01-04"))))

        startUseCase(repo)()

        assertEquals(date("2026-01-08"), repo.periods.first().endDate) // 기본 5일
        assertNull(repo.periods.last().endDate)
    }

    @Test
    fun 시작한_지_얼마_안_됐으면_중복_시작으로_막는다() = runTest {
        val repo = FakePeriodRepository(listOf(Period(startDate = date("2026-01-25"))))
        assertEquals(RecordPeriodStartResult.AlreadyOngoing, startUseCase(repo)())
    }

    @Test
    fun 기존_기간과_겹치면_막는다() = runTest {
        val repo = FakePeriodRepository(listOf(Period(startDate = date("2026-01-04"), endDate = date("2026-01-08"))))
        assertEquals(RecordPeriodStartResult.Overlaps, startUseCase(repo)(date("2026-01-06")))
    }

    @Test
    fun 과거를_뒤늦게_기록하면_종료일을_자동으로_채운다() = runTest {
        val repo = FakePeriodRepository(listOf(Period(startDate = date("2026-01-30"))))

        startUseCase(repo)(date("2026-01-02"))

        assertEquals(Period(id = 2, startDate = date("2026-01-02"), endDate = date("2026-01-06")), repo.periods.first())
        assertNull(repo.periods.last().endDate)
    }

    @Test
    fun 종료_기록() = runTest {
        val repo = FakePeriodRepository(listOf(Period(startDate = date("2026-01-28"))))

        assertEquals(RecordPeriodEndResult.Success, endUseCase(repo)())
        assertEquals(today, repo.periods.single().endDate)
    }

    @Test
    fun 진행_중인_생리가_없으면_종료할_수_없다() = runTest {
        val repo = FakePeriodRepository(listOf(Period(startDate = date("2026-01-01"), endDate = date("2026-01-05"))))
        assertEquals(RecordPeriodEndResult.NoOngoingPeriod, endUseCase(repo)())
    }

    @Test
    fun 시작일보다_빠른_종료는_막는다() = runTest {
        val repo = FakePeriodRepository(listOf(Period(startDate = date("2026-01-28"))))
        assertEquals(RecordPeriodEndResult.BeforeStart, endUseCase(repo)(date("2026-01-27")))
    }

    @Test
    fun 기록하면_상태_Flow가_갱신된다() = runTest {
        val repo = FakePeriodRepository()
        val observe = ObserveCycleStatusUseCase(repo, dateProvider)

        assertNull(observe().first())

        startUseCase(repo)()
        val status = observe().first()!!
        assertEquals(1, status.cycleDay)
        assertEquals(CyclePhase.MENSTRUAL, status.phase)
    }
}
