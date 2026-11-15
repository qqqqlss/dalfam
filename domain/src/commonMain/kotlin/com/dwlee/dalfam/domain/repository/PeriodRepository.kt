package com.dwlee.dalfam.domain.repository

import com.dwlee.dalfam.domain.model.Period
import kotlinx.coroutines.flow.Flow

/**
 * 생리 기록 저장소. 구현(Room 등)은 data 계층에 둔다.
 */
interface PeriodRepository {
    /** 모든 기록을 시작일 오름차순으로 관찰한다. 기록이 바뀌면 새 목록이 흐른다. */
    fun observePeriods(): Flow<List<Period>>

    /** 시작일 오름차순 스냅샷. */
    suspend fun getPeriods(): List<Period>

    /** 새 기록을 저장하고 id를 반환한다. */
    suspend fun insert(period: Period): Long

    suspend fun update(period: Period)

    suspend fun delete(id: Long)
}
