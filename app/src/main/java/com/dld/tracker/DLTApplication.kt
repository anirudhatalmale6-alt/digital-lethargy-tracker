package com.dld.tracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.dld.tracker.data.local.DLTDatabase
import com.dld.tracker.data.preferences.SecurePrefs
import com.dld.tracker.data.repository.UsageRepository

class DLTApplication : Application() {

    lateinit var database: DLTDatabase
        private set
    lateinit var securePrefs: SecurePrefs
        private set
    lateinit var usageRepository: UsageRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        securePrefs = SecurePrefs(this)
        database = DLTDatabase.getInstance(this, securePrefs)
        usageRepository = UsageRepository(
            usageEventDao = database.usageEventDao(),
            interactionEventDao = database.interactionEventDao(),
            sessionDao = database.sessionDao(),
            selfReportDao = database.selfReportDao(),
            reactionTestDao = database.reactionTestDao()
        )
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_desc)
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "dlt_tracking"
        lateinit var instance: DLTApplication
            private set
    }
}
