package com.dwlee.dalfam.di

import com.dwlee.dalfam.domain.common.time.DateProvider
import com.dwlee.dalfam.domain.common.time.SystemDateProvider
import com.dwlee.dalfam.domain.cycle.PeriodRepository
import com.dwlee.dalfam.domain.cycle.usecase.CalculateCycleStatsUseCase
import com.dwlee.dalfam.domain.cycle.usecase.GetCycleStatusUseCase
import com.dwlee.dalfam.domain.cycle.usecase.ObserveCycleStatusUseCase
import com.dwlee.dalfam.domain.cycle.usecase.PredictCycleUseCase
import com.dwlee.dalfam.domain.cycle.usecase.RecordPeriodEndUseCase
import com.dwlee.dalfam.domain.cycle.usecase.RecordPeriodStartUseCase
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

/**
 * domain 레이어 등록.
 *
 * domain 모듈은 Koin을 모르는 순수 Kotlin으로 둔다(ADR 0006). 그래서 UseCase 클래스에
 * `@Factory`를 직접 붙일 수 없고, 여기서 "이렇게 만들어라"는 함수를 대신 적는다.
 * Hilt에서 남의 클래스(Retrofit, Room 등)를 `@Provides` 함수로 등록하는 것과 같은 모양이다.
 *
 * - 함수 파라미터 = 필요한 의존성. Koin이 그래프에서 찾아 넣는다.
 * - UseCase는 상태가 없어서 매번 새로 만들어도 되므로 `@Factory`.
 */
@Module
class DomainModule {

    @Singleton
    fun dateProvider(): DateProvider = SystemDateProvider()
    @Factory
    fun calculateCycleStats() = CalculateCycleStatsUseCase()

    @Factory
    fun predictCycle() = PredictCycleUseCase()

    @Factory
    fun getCycleStatus(
        calculateStats: CalculateCycleStatsUseCase,
        predictCycle: PredictCycleUseCase,
    ) = GetCycleStatusUseCase(calculateStats = calculateStats, predictCycle = predictCycle)

    @Factory
    fun observeCycleStatus(
        repository: PeriodRepository,
        dateProvider: DateProvider,
        getCycleStatus: GetCycleStatusUseCase,
    ) = ObserveCycleStatusUseCase(repository = repository, dateProvider = dateProvider, getCycleStatus = getCycleStatus)

    @Factory
    fun recordPeriodStart(
        repository: PeriodRepository,
        dateProvider: DateProvider,
        calculateStats: CalculateCycleStatsUseCase,
    ) = RecordPeriodStartUseCase(repository = repository, dateProvider = dateProvider, calculateStats = calculateStats)

    @Factory
    fun recordPeriodEnd(
        repository: PeriodRepository,
        dateProvider: DateProvider,
    ) = RecordPeriodEndUseCase(repository = repository, dateProvider = dateProvider)
}
