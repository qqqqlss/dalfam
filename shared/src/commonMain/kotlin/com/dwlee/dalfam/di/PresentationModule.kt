package com.dwlee.dalfam.di

import com.dwlee.dalfam.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * 화면(ViewModel) 등록.
 *
 * `viewModel { }`은 `factory`와 비슷하지만, 화면 생명주기에 맞춰 보관된다.
 * 화면 회전에도 같은 인스턴스가 유지되고, 화면이 완전히 닫히면 버려진다.
 */
val presentationModule = module {
    viewModel {
        HomeViewModel(
            observeCycleStatus = get(),
            recordPeriodStart = get(),
            recordPeriodEnd = get(),
        )
    }
}
