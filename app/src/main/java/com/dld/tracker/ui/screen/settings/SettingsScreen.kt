package com.dld.tracker.ui.screen.settings

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dld.tracker.DLTApplication
import com.dld.tracker.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = DLTApplication.instance
    val scope = rememberCoroutineScope()

    var isTracking by remember { mutableStateOf(app.securePrefs.isTrackingEnabled()) }
    var apiEndpoint by remember { mutableStateOf(app.securePrefs.getApiEndpoint()) }
    var autoSync by remember { mutableStateOf(app.securePrefs.isAutoSyncEnabled()) }
    var showClearDialog by remember { mutableStateOf(false) }

    val hasUsageAccess = remember {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        mode == AppOpsManager.MODE_ALLOWED
    }

    val isBatteryOptimized = remember {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val ignoring = pm.isIgnoringBatteryOptimizations(context.packageName)
        !ignoring
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = DarkNavy
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Permissions Section
            Text("Permissions", fontWeight = FontWeight.Bold, color = TextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Usage Access
            SettingsCard(
                title = "Usage Access",
                subtitle = if (hasUsageAccess) "Granted" else "Required for app tracking",
                icon = Icons.Default.BarChart,
                iconColor = if (hasUsageAccess) AccentGreen else AccentRed,
                action = {
                    if (!hasUsageAccess) {
                        TextButton(onClick = {
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        }) {
                            Text("Grant", color = AccentCyan)
                        }
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentGreen)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Accessibility
            SettingsCard(
                title = "Accessibility Service",
                subtitle = "Required for tap/scroll tracking",
                icon = Icons.Default.TouchApp,
                iconColor = AccentOrange,
                action = {
                    TextButton(onClick = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }) {
                        Text("Open", color = AccentCyan)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Battery Optimization
            if (isBatteryOptimized) {
                SettingsCard(
                    title = "Battery Optimization",
                    subtitle = "Disable to prevent tracking interruptions",
                    icon = Icons.Default.BatteryAlert,
                    iconColor = AccentYellow,
                    action = {
                        TextButton(onClick = {
                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            context.startActivity(intent)
                        }) {
                            Text("Fix", color = AccentCyan)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tracking Section
            Text("Tracking", fontWeight = FontWeight.Bold, color = TextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            SettingsCard(
                title = "Tracking Service",
                subtitle = if (isTracking) "Active — collecting data" else "Paused",
                icon = if (isTracking) Icons.Default.PlayArrow else Icons.Default.Pause,
                iconColor = if (isTracking) AccentGreen else AccentRed,
                action = {
                    Switch(
                        checked = isTracking,
                        onCheckedChange = {
                            isTracking = it
                            app.securePrefs.setTrackingEnabled(it)
                            if (it) {
                                com.dld.tracker.service.UsagePollingService.start(context)
                            } else {
                                com.dld.tracker.service.UsagePollingService.stop(context)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentGreen,
                            checkedTrackColor = AccentGreen.copy(alpha = 0.3f)
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Data Sync Section
            Text("Data Sync (Optional)", fontWeight = FontWeight.Bold, color = TextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = apiEndpoint,
                        onValueChange = {
                            apiEndpoint = it
                            app.securePrefs.setApiEndpoint(it)
                        },
                        label = { Text("REST Endpoint URL") },
                        placeholder = { Text("https://your-server.com/api/data") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = TextMuted,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedLabelColor = AccentCyan
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto-sync batches", color = TextPrimary, fontSize = 14.sp)
                        Switch(
                            checked = autoSync,
                            onCheckedChange = {
                                autoSync = it
                                app.securePrefs.setAutoSyncEnabled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AccentCyan,
                                checkedTrackColor = AccentCyan.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Danger Zone
            Text("Data Management", fontWeight = FontWeight.Bold, color = AccentRed, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showClearDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear All Data")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Digital Lethargy Tracker v1.0.0", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("DLD-1 Research Dataset Tool", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "All data is encrypted locally with SQLCipher. No data leaves your device unless you explicitly export or enable sync.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = { showClearDialog = false },
                title = { Text("Clear All Data?", color = TextPrimary) },
                text = { Text("This will permanently delete all collected usage events, interactions, sessions, self-reports, and reaction tests. This cannot be undone.", color = TextSecondary) },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            app.usageRepository.clearAllData()
                            showClearDialog = false
                        }
                    }) {
                        Text("Delete All", color = AccentRed)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = SurfaceCard
            )
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    action: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, color = TextPrimary, fontSize = 14.sp)
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
            action()
        }
    }
}
