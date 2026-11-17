package com.dwlee.dalfam.data.cycle

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlinx.datetime.LocalDate

/**
 * 생리 기록 테이블. 날짜는 ISO-8601 문자열(`2026-01-31`)로 저장해 정렬과 DB 확인이 쉽게 했다.
 *
 * 같은 날 시작한 기록이 둘일 수 없으므로 시작일에 unique 인덱스를 둔다.
 */
@Entity(
    tableName = "period",
    indices = [Index(value = ["start_date"], unique = true)],
)
internal data class PeriodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "start_date")
    val startDate: LocalDate,
    @ColumnInfo(name = "end_date")
    val endDate: LocalDate?,
)
