package com.dwlee.dalfam.di

/**
 * Swift에서 부르는 진입점. Swift에서는 `KoinIosKt.startKoinForIos()`로 보인다.
 * (Kotlin 기본값 인자는 Swift로 내보내지지 않아서 인자 없는 함수를 따로 둔다.)
 */
fun startKoinForIos() = initKoin()
