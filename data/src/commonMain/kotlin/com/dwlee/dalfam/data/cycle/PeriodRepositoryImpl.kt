package com.dwlee.dalfam.data.cycle

import com.dwlee.dalfam.domain.model.Period
import com.dwlee.dalfam.domain.repository.PeriodRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Room 기반 [PeriodRepository]. 호출하는 쪽은 스레드를 신경 쓰지 않아도 된다(main-safe, ADR 0007).
 *
 * @param ioDispatcher DB 작업을 돌릴 Dispatcher. 테스트에서 바꿀 수 있도록 주입받는다.
 */
internal class PeriodRepositoryImpl(
    private val dao: PeriodDao,
    private val ioDispatcher: CoroutineDispatcher,
) : PeriodRepository {

    override fun observePeriods(): Flow<List<Period>> =
        dao.observeAll()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    override suspend fun getPeriods(): List<Period> = withContext(ioDispatcher) {
        dao.getAll().map { it.toDomain() }
    }

    override suspend fun insert(period: Period): Long = withContext(ioDispatcher) {
        dao.insert(period.toEntity())
    }

    override suspend fun update(period: Period) = withContext(ioDispatcher) {
        dao.update(period.toEntity())
    }

    override suspend fun delete(id: Long) = withContext(ioDispatcher) {
        dao.deleteById(id)
    }
}
