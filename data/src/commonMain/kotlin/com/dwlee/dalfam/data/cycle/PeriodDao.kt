package com.dwlee.dalfam.data.cycle

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface PeriodDao {

    @Query("SELECT * FROM period ORDER BY start_date ASC")
    fun observeAll(): Flow<List<PeriodEntity>>

    @Query("SELECT * FROM period ORDER BY start_date ASC")
    suspend fun getAll(): List<PeriodEntity>

    @Insert
    suspend fun insert(entity: PeriodEntity): Long

    @Update
    suspend fun update(entity: PeriodEntity)

    @Query("DELETE FROM period WHERE id = :id")
    suspend fun deleteById(id: Long)
}
