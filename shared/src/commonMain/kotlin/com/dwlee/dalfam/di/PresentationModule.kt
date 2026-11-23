package com.dwlee.dalfam.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

/**
 * 화면(ViewModel) 등록.
 *
 * ViewModel 클래스에는 `@KoinViewModel`을 직접 붙이고(예: `HomeViewModel`),
 * 여기서는 그 패키지를 훑기만 한다. 화면이 늘어나도 이 파일은 고칠 필요가 없다.
 * (DSL에서는 화면마다 `viewModel { ... }` 줄을 추가해야 했다.)
 */
@Module
@ComponentScan("com.dwlee.dalfam.home")
class PresentationModule
