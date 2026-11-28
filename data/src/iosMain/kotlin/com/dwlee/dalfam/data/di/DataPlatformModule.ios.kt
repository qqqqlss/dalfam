package com.dwlee.dalfam.data.di

import androidx.room3.RoomDatabase
import com.dwlee.dalfam.data.database.DalfamDatabase
import com.dwlee.dalfam.data.database.dalfamDatabaseBuilder
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

/**
 * iOS용 DB 빌더. Context 없이 Application Support 경로에 만든다.
 *
 * `@Configuration`: Android 쪽과 같은 이유로, `@KoinApplication`이 자동으로 찾아서 넣는다.
 */
@Module
@Configuration
class DataPlatformModule {

    @Singleton
    internal fun databaseBuilder(): RoomDatabase.Builder<DalfamDatabase> = dalfamDatabaseBuilder()
}
