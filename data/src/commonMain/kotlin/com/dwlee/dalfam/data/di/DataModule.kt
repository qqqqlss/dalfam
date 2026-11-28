package com.dwlee.dalfam.data.di

import androidx.room3.RoomDatabase
import com.dwlee.dalfam.data.cycle.PeriodDao
import com.dwlee.dalfam.data.database.DalfamDatabase
import com.dwlee.dalfam.data.database.buildDalfamDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Qualifier
import org.koin.core.annotation.Singleton

/**
 * DB 작업용 Dispatcher의 이름표(qualifier).
 *
 * `CoroutineDispatcher` 타입은 Default·Main 등 여러 개가 있을 수 있어서,
 * 타입만으로 찾으면 어느 것인지 모호하다. 어노테이션으로 구분한다.
 *
 * DSL 시절의 `named("io")`는 문자열이라 오타가 나도 실행해야 알 수 있었다.
 * 어노테이션은 클래스라서 오타가 나면 컴파일이 안 된다. (Hilt의 `@Qualifier`와 같은 방식)
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/**
 * data 레이어의 공통 등록.
 *
 * - `@ComponentScan`: 이 패키지(`com.dwlee.dalfam.data`)와 하위 패키지에서
 *   `@Singleton`·`@Factory`가 붙은 **클래스**를 찾아 등록한다. (예: `PeriodRepositoryImpl`)
 * - 클래스 안의 `@Singleton` **함수**: 우리가 생성자를 고칠 수 없는 타입(Dispatcher, Room DB)을
 *   만드는 방법을 알려 준다. Hilt의 `@Provides`와 같은 역할이다.
 *   함수 파라미터는 Koin이 그래프에서 찾아 넣어 준다.
 *
 * 함수가 `internal`인 이유: 반환 타입(`DalfamDatabase`, `PeriodDao`)이 `internal`이라
 * 공개 함수로는 내보낼 수 없다. 플러그인이 만드는 등록 코드도 같은 Gradle 모듈 안에 생기므로
 * `internal`이어도 호출할 수 있다. 밖에서는 클래스 이름(`DataModule`)만 보인다.
 *
 * DB를 만드는 플랫폼별 빌더(`RoomDatabase.Builder<DalfamDatabase>`)는
 * androidMain / iosMain의 `DataPlatformModule`이 제공한다.
 */
@Module
@ComponentScan("com.dwlee.dalfam.data")
class DataModule {

    @Singleton
    @IoDispatcher
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO

    // DB는 앱 전체에 하나만 있어야 하므로 @Singleton.
    // DSL에서는 get<CoroutineDispatcher>(IoDispatcher)처럼 타입을 직접 적어야 했지만,
    // 여기서는 파라미터 타입과 이름표가 곧 "무엇을 찾을지"가 된다.
    @Singleton
    internal fun database(
        builder: RoomDatabase.Builder<DalfamDatabase>,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): DalfamDatabase = builder.buildDalfamDatabase(queryContext = ioDispatcher)

    @Singleton
    internal fun periodDao(database: DalfamDatabase): PeriodDao = database.periodDao()
}
