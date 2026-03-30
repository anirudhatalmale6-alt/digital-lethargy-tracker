package com.dld.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dld.tracker.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insert(session: SessionEntity): Long

    @Update
    suspend fun update(session: SessionEntity)

    @Query("SELECT * FROM sessions WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSession(): SessionEntity?

    @Query("SELECT * FROM sessions ORDER BY startTimestamp DESC LIMIT :limit")
    fun getRecent(limit: Int = 50): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE startTimestamp BETWEEN :startMs AND :endMs ORDER BY startTimestamp ASC")
    suspend fun getByTimeRange(startMs: Long, endMs: Long): List<SessionEntity>

    @Query("SELECT COUNT(*) FROM sessions")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT SUM(durationMs) FROM sessions WHERE startTimestamp >= :sinceMs")
    suspend fun getTotalDurationSince(sinceMs: Long): Long?

    @Query("DELETE FROM sessions")
    suspend fun deleteAll()
}
