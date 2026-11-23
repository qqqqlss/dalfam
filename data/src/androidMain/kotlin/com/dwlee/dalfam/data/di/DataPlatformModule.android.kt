package com.dwlee.dalfam.data.di

import com.dwlee.dalfam.data.database.dalfamDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** Android용 DB 빌더. Context는 앱 시작 시 `androidContext(...)`로 넘긴 것을 쓴다. */
val dataPlatformModule = module {
    single { dalfamDatabaseBuilder(androidContext()) }
}
