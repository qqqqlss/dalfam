package com.dwlee.dalfam.data.di

import androidx.room3.Room
import com.dwlee.dalfam.data.database.DalfamDatabase
import com.dwlee.dalfam.domain.model.Period
import com.dwlee.dalfam.domain.repository.PeriodRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [dataModule]로 조립한 [PeriodRepository]가 실제 DB까지 연결되는지 확인한다.
 *
 * 플랫폼 빌더(`dataPlatformModule`) 자리에 메모리 DB 빌더를 넣어서,
 * 파일을 만들지 않고 나머지 조립(Dispatcher 이름표, DAO, Repository)을 그대로 검증한다.
 */
class DataModuleTest {

    @Test
    fun Koin으로_꺼낸_Repository로_저장하고_조회한다() = runTest {
        val koin = koinApplication {
            modules(
                dataModule,
                module { single { Room.inMemoryDatabaseBuilder<DalfamDatabase>() } },
            )
        }.koin

        val repository = koin.get<PeriodRepository>()
        val id = repository.insert(Period(startDate = LocalDate.parse("2026-01-01")))

        assertEquals(listOf(id), repository.getPeriods().map { it.id })

        koin.get<DalfamDatabase>().close()
        koin.close()
    }
}
