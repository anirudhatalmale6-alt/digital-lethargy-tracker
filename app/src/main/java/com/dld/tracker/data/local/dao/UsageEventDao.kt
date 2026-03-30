package com.dld.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dld.tracker.data.local.entity.UsageEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageEventDao {
    @Insert
    suspend fun insert(event: UsageEventEntity): Long

    @Query("SELECT * FROM usage_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int = 100): Flow<List<UsageEventEntity>>

    @Query("SELECT * FROM usage_events WHERE timestamp BETWEEN :startMs AND :endMs ORDER BY timestamp ASC")
    suspend fun getByTimeRange(startMs: Long, endMs: Long): List<UsageEventEntity>

    @Query("SELECT COUNT(*) FROM usage_events")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM usage_events WHERE timestamp BETWEEN :startMs AND :endMs")
    suspend fun getCountByTimeRange(startMs: Long, endMs: Long): Int

    @Query("SELECT SUM(dwellMs) FROM usage_events WHERE timestamp >= :sinceMs")
    suspend fun getTotalDwellSince(sinceMs: Long): Long?

    @Query("SELECT DISTINCT appPackage FROM usage_events WHERE timestamp >= :sinceMs")
    suspend fun getDistinctAppsSince(sinceMs: Long): List<String>

    @Query("DELETE FROM usage_events")
    suspend fun deleteAll()

    @Query("DELETE FROM usage_events WHERE timestamp < :beforeMs")
    suspend fun deleteBefore(beforeMs: Long)
}
