package com.dwlee.dalfam.data.database

import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.dwlee.dalfam.data.cycle.PeriodDao
import com.dwlee.dalfam.data.cycle.PeriodEntity
import kotlin.coroutines.CoroutineContext

/**
 * 앱 로컬 DB. 건강 데이터라 기기 밖으로 내보내지 않는다(ADR 0002).
 *
 * 스키마를 바꾸면 `version`을 올리고 마이그레이션을 추가한다. 스키마는 `data/schemas/`에 내보내 커밋한다.
 */
@Database(
    entities = [PeriodEntity::class],
    version = 1,
    exportSchema = true,
)
@ColumnTypeConverters(LocalDateConverter::class)
@ConstructedBy(DalfamDatabaseConstructor::class)
internal abstract class DalfamDatabase : RoomDatabase() {
    abstract fun periodDao(): PeriodDao
}

/** Room 컴파일러가 플랫폼마다 `actual`을 생성한다. */
@Suppress("KotlinNoActualForExpect")
internal expect object DalfamDatabaseConstructor : RoomDatabaseConstructor<DalfamDatabase> {
    override fun initialize(): DalfamDatabase
}

internal const val DATABASE_FILE_NAME = "dalfam.db"

/**
 * 플랫폼별 빌더(`dalfamDatabaseBuilder`)에 공통 설정을 적용해 DB를 만든다.
 *
 * @param queryContext Room이 쿼리를 실행할 컨텍스트. 주입받은 IO Dispatcher를 넘긴다.
 */
internal fun RoomDatabase.Builder<DalfamDatabase>.buildDalfamDatabase(
    queryContext: CoroutineContext,
): DalfamDatabase =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(queryContext)
        .build()
