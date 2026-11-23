package com.dwlee.dalfam.data.di

import androidx.room3.RoomDatabase
import com.dwlee.dalfam.data.cycle.PeriodRepositoryImpl
import com.dwlee.dalfam.data.database.DalfamDatabase
import com.dwlee.dalfam.data.database.buildDalfamDatabase
import com.dwlee.dalfam.domain.cycle.PeriodRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * DB 작업용 Dispatcher의 이름표(qualifier).
 *
 * `CoroutineDispatcher` 타입은 Default·Main 등 여러 개가 있을 수 있어서,
 * 타입만으로 찾으면 어느 것인지 모호하다. 이름을 붙여 구분한다.
 */
val IoDispatcher = named("io")

/**
 * data 레이어의 공통 등록.
 *
 * 구현 클래스가 전부 `internal`이라 이 모듈 밖에서는 등록할 수 없다.
 * 밖에서는 [PeriodRepository] 인터페이스로만 꺼내 쓴다.
 *
 * DB를 만드는 플랫폼별 빌더(`RoomDatabase.Builder<DalfamDatabase>`)는
 * androidMain / iosMain의 `dataPlatformModule`이 제공한다.
 */
val dataModule = module {
    single<CoroutineDispatcher>(IoDispatcher) { Dispatchers.IO }

    // DB는 앱 전체에 하나만 있어야 하므로 single.
    single<DalfamDatabase> {
        get<RoomDatabase.Builder<DalfamDatabase>>()
            // 파라미터 타입이 CoroutineContext라서 그냥 get()하면 Koin은 CoroutineContext를 찾는다.
            // 등록한 타입(CoroutineDispatcher)을 명시해야 한다. Koin은 정확한 타입으로 찾는다.
            .buildDalfamDatabase(queryContext = get<CoroutineDispatcher>(IoDispatcher))
    }
    single { get<DalfamDatabase>().periodDao() }

    // 인터페이스 타입으로 등록해야 domain 쪽에서 get<PeriodRepository>()로 찾을 수 있다.
    single<PeriodRepository> {
        PeriodRepositoryImpl(
            dao = get(),
            ioDispatcher = get(IoDispatcher),
        )
    }
}
