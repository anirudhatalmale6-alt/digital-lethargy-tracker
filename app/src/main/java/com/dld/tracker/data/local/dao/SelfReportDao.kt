package com.dld.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dld.tracker.data.local.entity.SelfReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SelfReportDao {
    @Insert
    suspend fun insert(report: SelfReportEntity): Long

    @Query("SELECT * FROM self_reports ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int = 50): Flow<List<SelfReportEntity>>

    @Query("SELECT * FROM self_reports WHERE timestamp BETWEEN :startMs AND :endMs ORDER BY timestamp ASC")
    suspend fun getByTimeRange(startMs: Long, endMs: Long): List<SelfReportEntity>

    @Query("SELECT COUNT(*) FROM self_reports")
    fun getTotalCount(): Flow<Int>

    @Query("DELETE FROM self_reports")
    suspend fun deleteAll()
}
