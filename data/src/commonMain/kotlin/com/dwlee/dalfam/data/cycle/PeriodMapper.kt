package com.dwlee.dalfam.data.cycle

import com.dwlee.dalfam.domain.model.Period

internal fun PeriodEntity.toDomain(): Period = Period(
    id = id,
    startDate = startDate,
    endDate = endDate,
)

internal fun Period.toEntity(): PeriodEntity = PeriodEntity(
    id = id,
    startDate = startDate,
    endDate = endDate,
)
