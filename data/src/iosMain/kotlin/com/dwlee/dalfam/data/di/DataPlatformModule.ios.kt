package com.dwlee.dalfam.data.di

import com.dwlee.dalfam.data.database.dalfamDatabaseBuilder
import org.koin.dsl.module

/** iOS용 DB 빌더. Context 없이 Application Support 경로에 만든다. */
val dataPlatformModule = module {
    single { dalfamDatabaseBuilder() }
}
