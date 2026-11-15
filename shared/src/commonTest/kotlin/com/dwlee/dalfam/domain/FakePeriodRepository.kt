package com.dwlee.dalfam.domain

import com.dwlee.dalfam.domain.model.Period
import com.dwlee.dalfam.domain.repository.PeriodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class FakePeriodRepository(initial: List<Period> = emptyList()) : PeriodRepository {
    private var nextId = 1L
    private val state = MutableStateFlow(initial.map { if (it.id == 0L) it.copy(id = nextId++) else it })

    val periods: List<Period> get() = state.value.sortedBy { it.startDate }

    override fun observePeriods(): Flow<List<Period>> = state.map { list -> list.sortedBy { it.startDate } }

    override suspend fun getPeriods(): List<Period> = periods

    override suspend fun insert(period: Period): Long {
        val id = nextId++
        state.update { it + period.copy(id = id) }
        return id
    }

    override suspend fun update(period: Period) {
        state.update { list -> list.map { if (it.id == period.id) period else it } }
    }

    override suspend fun delete(id: Long) {
        state.update { list -> list.filterNot { it.id == id } }
    }
}

fun date(iso: String): LocalDate = LocalDate.parse(iso)
