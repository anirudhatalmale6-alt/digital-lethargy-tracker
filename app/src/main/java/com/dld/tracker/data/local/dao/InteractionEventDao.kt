package com.dld.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dld.tracker.data.local.entity.InteractionEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InteractionEventDao {
    @Insert
    suspend fun insert(event: InteractionEventEntity): Long

    @Query("SELECT * FROM interaction_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int = 100): Flow<List<InteractionEventEntity>>

    @Query("SELECT * FROM interaction_events WHERE timestamp BETWEEN :startMs AND :endMs ORDER BY timestamp ASC")
    suspend fun getByTimeRange(startMs: Long, endMs: Long): List<InteractionEventEntity>

    @Query("SELECT COUNT(*) FROM interaction_events")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM interaction_events WHERE timestamp BETWEEN :startMs AND :endMs")
    suspend fun getCountByTimeRange(startMs: Long, endMs: Long): Int

    @Query("SELECT COUNT(*) FROM interaction_events WHERE eventType = :type AND timestamp >= :sinceMs")
    suspend fun getCountByTypeSince(type: String, sinceMs: Long): Int

    @Query("DELETE FROM interaction_events")
    suspend fun deleteAll()
}
