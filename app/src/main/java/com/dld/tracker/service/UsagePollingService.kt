package com.dld.tracker.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.dld.tracker.DLTApplication
import com.dld.tracker.R
import com.dld.tracker.data.local.entity.SessionEntity
import com.dld.tracker.data.local.entity.UsageEventEntity
import com.dld.tracker.ui.MainActivity
import kotlinx.coroutines.*

class UsagePollingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isPolling = false
    private var currentPackage: String? = null
    private var lastSwitchTime: Long = 0
    private var currentSessionId: Long? = null

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    serviceScope.launch { endCurrentSession() }
                }
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                    serviceScope.launch { startNewSession() }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopPolling()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        startForeground(NOTIFICATION_ID, createNotification())
        startPolling()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopPolling()
        try {
            unregisterReceiver(screenReceiver)
        } catch (_: Exception) {}
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startPolling() {
        if (isPolling) return
        isPolling = true
        lastSwitchTime = System.currentTimeMillis()

        serviceScope.launch {
            startNewSession()
            val app = DLTApplication.instance
            val pollingInterval = app.securePrefs.getPollingIntervalMs()

            while (isActive && isPolling) {
                try {
                    pollUsageStats()
                } catch (e: Exception) {
                    // Continue polling even if one poll fails
                }
                delay(pollingInterval)
            }
        }
    }

    private fun stopPolling() {
        isPolling = false
        serviceScope.launch {
            recordCurrentApp()
            endCurrentSession()
        }
    }

    private suspend fun pollUsageStats() {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return

        val now = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(now - 3000, now)
        val event = UsageEvents.Event()
        var latestForegroundPackage: String? = null
        var latestTimestamp = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND && event.timeStamp > latestTimestamp) {
                latestForegroundPackage = event.packageName
                latestTimestamp = event.timeStamp
            }
        }

        if (latestForegroundPackage != null && latestForegroundPackage != currentPackage) {
            recordCurrentApp()
            currentPackage = latestForegroundPackage
            lastSwitchTime = now
        }
    }

    private suspend fun recordCurrentApp() {
        val pkg = currentPackage ?: return
        val now = System.currentTimeMillis()
        val dwell = now - lastSwitchTime
        if (dwell < 500) return // Skip very short dwells

        val app = DLTApplication.instance
        app.usageRepository.insertUsageEvent(
            UsageEventEntity(
                timestamp = lastSwitchTime,
                appPackage = pkg,
                dwellMs = dwell,
                sessionId = currentSessionId
            )
        )
    }

    private suspend fun startNewSession() {
        endCurrentSession()
        val app = DLTApplication.instance
        val session = SessionEntity(
            startTimestamp = System.currentTimeMillis(),
            isActive = true
        )
        currentSessionId = app.usageRepository.insertSession(session)
    }

    private suspend fun endCurrentSession() {
        val sessionId = currentSessionId ?: return
        val app = DLTApplication.instance
        val session = app.usageRepository.getActiveSession() ?: return
        val now = System.currentTimeMillis()
        app.usageRepository.updateSession(
            session.copy(
                endTimestamp = now,
                durationMs = now - session.startTimestamp,
                isActive = false
            )
        )
        currentSessionId = null
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, UsagePollingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, DLTApplication.CHANNEL_ID)
            .setContentTitle("DL Tracker Active")
            .setContentText("Collecting usage data for research")
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.dld.tracker.STOP_TRACKING"

        fun start(context: Context) {
            val intent = Intent(context, UsagePollingService::class.java)
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, UsagePollingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
