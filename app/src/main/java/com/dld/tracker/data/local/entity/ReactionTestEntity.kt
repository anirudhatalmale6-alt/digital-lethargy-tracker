package com.dld.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reaction_tests")
data class ReactionTestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val reactionTimeMs: Long,
    val wasCorrect: Boolean,
    val sessionId: Long? = null
)
