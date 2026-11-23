package com.dwlee.dalfam.data.di

import android.content.Context
import androidx.room3.RoomDatabase
import com.dwlee.dalfam.data.database.DalfamDatabase
import com.dwlee.dalfam.data.database.dalfamDatabaseBuilder
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

/**
 * Android용 DB 빌더.
 *
 * `Context` 파라미터는 앱 시작 시 `androidContext(...)`로 넘긴 것을 Koin이 넣어 준다.
 * (DSL에서는 `androidContext()`를 직접 불렀다. Hilt라면 `@ApplicationContext`를 붙이는 자리다.)
 *
 * `@Configuration`: 누가 `includes`에 적지 않아도 `@KoinApplication`이 이 모듈을 찾아서 넣는다.
 * 플랫폼마다 다른 클래스를 공통 코드에서 이름으로 부를 수 없어서 이 방식을 쓴다.
 * Hilt의 `@InstallIn(SingletonComponent::class)`와 같은 역할이다.
 */
@Module
@Configuration
class DataPlatformModule {

    @Singleton
    internal fun databaseBuilder(context: Context): RoomDatabase.Builder<DalfamDatabase> =
        dalfamDatabaseBuilder(context)
}
