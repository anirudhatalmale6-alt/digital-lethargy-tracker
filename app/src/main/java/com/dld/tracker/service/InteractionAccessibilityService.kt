package com.dld.tracker.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.dld.tracker.DLTApplication
import com.dld.tracker.data.local.entity.InteractionEventEntity
import kotlinx.coroutines.*

class InteractionAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val app = try {
            DLTApplication.instance
        } catch (_: Exception) {
            return
        }

        // Check if tracking is paused
        if (!app.securePrefs.isTrackingEnabled()) return

        val eventType = when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> "tap"
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> "scroll"
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> "screen_change"
            else -> return
        }

        val packageName = event.packageName?.toString() ?: "unknown"
        val screenId = event.className?.toString()

        serviceScope.launch {
            try {
                app.usageRepository.insertInteractionEvent(
                    InteractionEventEntity(
                        timestamp = System.currentTimeMillis(),
                        appPackage = packageName,
                        screenId = screenId,
                        eventType = eventType
                    )
                )
            } catch (_: Exception) {
                // Don't crash the accessibility service
            }
        }
    }

    override fun onInterrupt() {
        // Required override
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
