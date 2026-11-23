package com.dwlee.dalfam

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.dwlee.dalfam.home.HomeScreen

/**
 * Android·iOS 공통 최상위 화면.
 *
 * Koin은 각 플랫폼의 앱 시작점(DalfamApp, iOSApp)에서 이미 시작돼 있어야 한다.
 * 그래서 Koin이 없는 미리보기에서는 이 함수 대신 HomeContent를 직접 그린다.
 */
@Composable
fun App() {
    MaterialTheme {
        HomeScreen()
    }
}
