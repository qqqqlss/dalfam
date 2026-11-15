package com.dwlee.dalfam.domain.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/** "오늘"을 제공한다. 테스트에서 날짜를 고정하기 위해 인터페이스로 분리. */
fun interface DateProvider {
    fun today(): LocalDate
}

class SystemDateProvider(
    private val clock: Clock = Clock.System,
    private val timeZone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) : DateProvider {
    override fun today(): LocalDate = clock.todayIn(timeZone())
}
