package com.dwlee.dalfam.data.cycle

import androidx.room3.Room
import app.cash.turbine.test
import com.dwlee.dalfam.data.database.DalfamDatabase
import com.dwlee.dalfam.data.database.buildDalfamDatabase
import com.dwlee.dalfam.domain.cycle.model.Period
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/** 메모리 DB에 실제 SQLite(bundled)로 저장·조회해 Room 매핑과 쿼리를 검증한다. */
class PeriodRepositoryImplTest {
    private lateinit var database: DalfamDatabase
    private lateinit var repository: PeriodRepositoryImpl

    @BeforeTest
    fun setUp() {
        // Room이 SQLite를 실제 스레드에서 호출하므로 테스트에서도 실제 Dispatcher를 쓴다.
        database = Room.inMemoryDatabaseBuilder<DalfamDatabase>()
            .buildDalfamDatabase(Dispatchers.Default)
        repository = PeriodRepositoryImpl(database.periodDao(), Dispatchers.Default)
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun 저장하면_id를_발급하고_시작일_오름차순으로_조회된다() = runTest {
        val laterId = repository.insert(Period(startDate = date("2026-02-01")))
        val earlierId = repository.insert(Period(startDate = date("2026-01-01"), endDate = date("2026-01-05")))

        assertNotEquals(0L, laterId)
        assertNotEquals(laterId, earlierId)
        assertEquals(
            listOf(
                Period(id = earlierId, startDate = date("2026-01-01"), endDate = date("2026-01-05")),
                Period(id = laterId, startDate = date("2026-02-01"), endDate = null),
            ),
            repository.getPeriods(),
        )
    }

    @Test
    fun 날짜는_ISO_문자열_순서와_같게_정렬된다() = runTest {
        // 문자열 정렬이 날짜 정렬과 어긋나는 경우가 없는지(월·일 두 자리 패딩)
        listOf("2026-10-01", "2026-09-30", "2025-12-31").forEach {
            repository.insert(Period(startDate = date(it)))
        }

        assertEquals(
            listOf(date("2025-12-31"), date("2026-09-30"), date("2026-10-01")),
            repository.getPeriods().map { it.startDate },
        )
    }

    @Test
    fun 종료일을_수정한다() = runTest {
        val id = repository.insert(Period(startDate = date("2026-01-01")))

        repository.update(Period(id = id, startDate = date("2026-01-01"), endDate = date("2026-01-04")))

        assertEquals(date("2026-01-04"), repository.getPeriods().single().endDate)
    }

    @Test
    fun 삭제한다() = runTest {
        val keepId = repository.insert(Period(startDate = date("2026-01-01")))
        val deleteId = repository.insert(Period(startDate = date("2026-02-01")))

        repository.delete(deleteId)

        assertEquals(listOf(keepId), repository.getPeriods().map { it.id })
    }

    @Test
    fun 없는_id를_삭제해도_오류가_나지_않는다() = runTest {
        repository.delete(999)
        assertTrue(repository.getPeriods().isEmpty())
    }

    @Test
    fun 기록이_바뀌면_observePeriods가_새_목록을_흘린다() = runTest {
        repository.observePeriods().test {
            assertEquals(emptyList(), awaitItem())

            val id = repository.insert(Period(startDate = date("2026-01-01")))
            assertEquals(listOf(Period(id = id, startDate = date("2026-01-01"))), awaitItem())

            repository.update(Period(id = id, startDate = date("2026-01-01"), endDate = date("2026-01-05")))
            assertEquals(date("2026-01-05"), awaitItem().single().endDate)

            repository.delete(id)
            assertEquals(emptyList(), awaitItem())
        }
    }

    private fun date(iso: String) = LocalDate.parse(iso)
}
