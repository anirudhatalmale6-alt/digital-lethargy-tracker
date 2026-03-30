package com.dld.tracker.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.util.UUID

class SecurePrefs(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "dlt_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun isTrackingEnabled(): Boolean = prefs.getBoolean(KEY_TRACKING_ENABLED, false)

    fun setTrackingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TRACKING_ENABLED, enabled).apply()
    }

    fun isPrivacyAccepted(): Boolean = prefs.getBoolean(KEY_PRIVACY_ACCEPTED, false)

    fun setPrivacyAccepted(accepted: Boolean) {
        prefs.edit().putBoolean(KEY_PRIVACY_ACCEPTED, accepted).apply()
    }

    fun getDatabaseKey(): String {
        var key = prefs.getString(KEY_DB_ENCRYPTION, null)
        if (key == null) {
            key = UUID.randomUUID().toString() + UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DB_ENCRYPTION, key).apply()
        }
        return key
    }

    fun getApiEndpoint(): String = prefs.getString(KEY_API_ENDPOINT, "") ?: ""

    fun setApiEndpoint(url: String) {
        prefs.edit().putString(KEY_API_ENDPOINT, url).apply()
    }

    fun isAutoSyncEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_SYNC, false)

    fun setAutoSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
    }

    fun getPollingIntervalMs(): Long = prefs.getLong(KEY_POLLING_INTERVAL, 1500L)

    fun setPollingIntervalMs(interval: Long) {
        prefs.edit().putLong(KEY_POLLING_INTERVAL, interval).apply()
    }

    companion object {
        private const val KEY_TRACKING_ENABLED = "tracking_enabled"
        private const val KEY_PRIVACY_ACCEPTED = "privacy_accepted"
        private const val KEY_DB_ENCRYPTION = "db_encryption_key"
        private const val KEY_API_ENDPOINT = "api_endpoint"
        private const val KEY_AUTO_SYNC = "auto_sync"
        private const val KEY_POLLING_INTERVAL = "polling_interval"
    }
}
