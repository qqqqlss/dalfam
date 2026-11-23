package com.dwlee.dalfam.di

import com.dwlee.dalfam.domain.common.time.DateProvider
import com.dwlee.dalfam.domain.common.time.SystemDateProvider
import com.dwlee.dalfam.domain.cycle.usecase.CalculateCycleStatsUseCase
import com.dwlee.dalfam.domain.cycle.usecase.GetCycleStatusUseCase
import com.dwlee.dalfam.domain.cycle.usecase.ObserveCycleStatusUseCase
import com.dwlee.dalfam.domain.cycle.usecase.PredictCycleUseCase
import com.dwlee.dalfam.domain.cycle.usecase.RecordPeriodEndUseCase
import com.dwlee.dalfam.domain.cycle.usecase.RecordPeriodStartUseCase
import org.koin.dsl.module

/**
 * domain 레이어 등록.
 *
 * domain 모듈은 Koin을 모르는 순수 Kotlin으로 두기 위해, 등록은 여기(shared)에서 한다.
 *
 * UseCase는 상태가 없어서 매번 새로 만들어도 되므로 factory.
 * 생성자 기본값이 있어도 Koin이 만들 때는 쓰지 않으니, 필요한 것은 전부 get()으로 넘긴다.
 */
val domainModule = module {
    single<DateProvider> { SystemDateProvider() }

    factory { CalculateCycleStatsUseCase() }
    factory { PredictCycleUseCase() }
    factory { GetCycleStatusUseCase(calculateStats = get(), predictCycle = get()) }

    factory { ObserveCycleStatusUseCase(repository = get(), dateProvider = get(), getCycleStatus = get()) }
    factory { RecordPeriodStartUseCase(repository = get(), dateProvider = get(), calculateStats = get()) }
    factory { RecordPeriodEndUseCase(repository = get(), dateProvider = get()) }
}
