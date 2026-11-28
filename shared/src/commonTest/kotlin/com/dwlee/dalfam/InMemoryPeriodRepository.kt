package com.dwlee.dalfam

import com.dwlee.dalfam.domain.cycle.model.Period
import com.dwlee.dalfam.domain.cycle.PeriodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** 테스트용 메모리 저장소. Room처럼 기록이 바뀌면 observePeriods가 새 목록을 흘린다. */
class InMemoryPeriodRepository(initial: List<Period> = emptyList()) : PeriodRepository {
    private val periods = MutableStateFlow(initial.sortedBy { it.startDate })
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0) + 1

    override fun observePeriods(): Flow<List<Period>> = periods

    override suspend fun getPeriods(): List<Period> = periods.value

    override suspend fun insert(period: Period): Long {
        val id = nextId++
        periods.update { (it + period.copy(id = id)).sortedBy { p -> p.startDate } }
        return id
    }

    override suspend fun update(period: Period) {
        periods.update { list -> list.map { if (it.id == period.id) period else it } }
    }

    override suspend fun delete(id: Long) {
        periods.update { list -> list.filterNot { it.id == id } }
    }
}
