package com.dld.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interaction_events")
data class InteractionEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val appPackage: String,
    val screenId: String? = null,
    val eventType: String,
    val sessionId: Long? = null
)
