package com.dwlee.dalfam

import android.app.Application
import com.dwlee.dalfam.di.initKoin
import org.koin.android.ext.koin.androidContext

/** 앱 프로세스가 시작될 때 가장 먼저 실행된다. Koin은 여기서 한 번만 시작한다. */
class DalfamApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@DalfamApp)
        }
    }
}
