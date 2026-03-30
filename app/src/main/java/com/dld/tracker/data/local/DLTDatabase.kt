package com.dld.tracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dld.tracker.data.local.dao.*
import com.dld.tracker.data.local.entity.*
import com.dld.tracker.data.preferences.SecurePrefs

@Database(
    entities = [
        UsageEventEntity::class,
        InteractionEventEntity::class,
        SessionEntity::class,
        SelfReportEntity::class,
        ReactionTestEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DLTDatabase : RoomDatabase() {

    abstract fun usageEventDao(): UsageEventDao
    abstract fun interactionEventDao(): InteractionEventDao
    abstract fun sessionDao(): SessionDao
    abstract fun selfReportDao(): SelfReportDao
    abstract fun reactionTestDao(): ReactionTestDao

    companion object {
        @Volatile
        private var INSTANCE: DLTDatabase? = null

        fun getInstance(context: Context, securePrefs: SecurePrefs): DLTDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): DLTDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                DLTDatabase::class.java,
                "dlt_data.db"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
