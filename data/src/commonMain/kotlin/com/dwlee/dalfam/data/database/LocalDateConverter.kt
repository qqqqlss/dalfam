package com.dwlee.dalfam.data.database

import androidx.room3.ColumnTypeConverter
import kotlinx.datetime.LocalDate

/** [LocalDate] ↔ ISO-8601 문자열. 문자열 정렬 순서가 날짜 순서와 같다. */
internal object LocalDateConverter {

    @ColumnTypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @ColumnTypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)
}
