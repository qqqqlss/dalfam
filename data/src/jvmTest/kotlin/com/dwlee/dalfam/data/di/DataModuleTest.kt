package com.dwlee.dalfam.data.di

import androidx.room3.Room
import androidx.room3.RoomDatabase
import com.dwlee.dalfam.data.database.DalfamDatabase
import com.dwlee.dalfam.domain.cycle.PeriodRepository
import com.dwlee.dalfam.domain.cycle.model.Period
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import org.koin.plugin.module.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertEquals

/** 테스트용 플랫폼 모듈. 파일 대신 메모리 DB 빌더를 준다. */
@Module
class InMemoryDatabaseModule {

    @Singleton
    internal fun databaseBuilder(): RoomDatabase.Builder<DalfamDatabase> =
        Room.inMemoryDatabaseBuilder<DalfamDatabase>()
}

/**
 * 테스트용 시작점. 앱의 `DalfamKoinApp`처럼, 플러그인이 이 그래프를 **컴파일할 때** 검사한다.
 * 테스트 코드의 `koin.get<PeriodRepository>()`도 이 그래프에 있는지 확인한다.
 */
@KoinApplication(modules = [DataModule::class, InMemoryDatabaseModule::class])
object DataTestApp

/**
 * [DataModule]로 조립한 [PeriodRepository]가 실제 DB까지 연결되는지 확인한다.
 *
 * 플랫폼 빌더(`DataPlatformModule`) 자리에 메모리 DB 빌더를 넣어서,
 * 파일을 만들지 않고 나머지 조립(Dispatcher 이름표, DAO, Repository)을 그대로 검증한다.
 * jvm에는 `DataPlatformModule`이 없어서 메모리 빌더와 겹치지 않는다.
 *
 * 왜 commonTest가 아니라 jvmTest인가?
 * Koin Compiler Plugin 1.2.1은 **iOS 테스트 컴파일**에서 `@ComponentScan`으로 찾은 클래스
 * (`PeriodRepositoryImpl`)를 보지 못해 "정의가 없다"며 컴파일을 막는다(2026-11-23, Kotlin 2.4.21).
 * 앱 코드는 문제없다. iOS 메인 컴파일의 `startKoin` 검사는 통과한다.
 * 플러그인이 고쳐지면 commonTest로 되돌린다.
 */
class DataModuleTest {

    @Test
    fun Koin으로_꺼낸_Repository로_저장하고_조회한다() = runTest {
        val koin = koinApplication<DataTestApp>().koin

        val repository = koin.get<PeriodRepository>()
        val id = repository.insert(Period(startDate = LocalDate.parse("2026-01-01")))

        assertEquals(listOf(id), repository.getPeriods().map { it.id })

        koin.get<DalfamDatabase>().close()
        koin.close()
    }
}
