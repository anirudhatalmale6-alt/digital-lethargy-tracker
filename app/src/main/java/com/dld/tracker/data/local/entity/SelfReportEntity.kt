package com.dld.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "self_reports")
data class SelfReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val label: String,
    val sessionId: Long? = null,
    val notes: String? = null
)
