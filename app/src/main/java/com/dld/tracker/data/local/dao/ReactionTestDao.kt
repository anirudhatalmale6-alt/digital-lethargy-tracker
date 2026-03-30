package com.dld.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dld.tracker.data.local.entity.ReactionTestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReactionTestDao {
    @Insert
    suspend fun insert(test: ReactionTestEntity): Long

    @Query("SELECT * FROM reaction_tests ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int = 50): Flow<List<ReactionTestEntity>>

    @Query("SELECT AVG(reactionTimeMs) FROM reaction_tests WHERE wasCorrect = 1 AND timestamp >= :sinceMs")
    suspend fun getAvgReactionTimeSince(sinceMs: Long): Double?

    @Query("SELECT COUNT(*) FROM reaction_tests")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT * FROM reaction_tests WHERE timestamp BETWEEN :startMs AND :endMs ORDER BY timestamp ASC")
    suspend fun getByTimeRange(startMs: Long, endMs: Long): List<ReactionTestEntity>

    @Query("DELETE FROM reaction_tests")
    suspend fun deleteAll()
}
