package com.dld.tracker.ui.screen.export

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.dld.tracker.domain.usecase.ExportDataUseCase
import com.dld.tracker.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = DLTApplication.instance
    val exportUseCase = remember { ExportDataUseCase(app.usageRepository) }

    var exportFormat by remember { mutableStateOf("CSV") }
    var timeRange by remember { mutableStateOf("today") }
    var exportStatus by remember { mutableStateOf("") }
    var isExporting by remember { mutableStateOf(false) }
    var rowCount by remember { mutableIntStateOf(0) }

    fun getTimeRange(): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        val start = when (timeRange) {
            "1h" -> now - 3_600_000L
            "6h" -> now - 21_600_000L
            "today" -> {
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.timeInMillis
            }
            "7d" -> now - 604_800_000L
            "30d" -> now - 2_592_000_000L
            "all" -> 0L
            else -> now - 86_400_000L
        }
        return Pair(start, now)
    }

    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                scope.launch {
                    isExporting = true
                    try {
                        val (start, end) = getTimeRange()
                        rowCount = exportUseCase.exportToCsv(context, uri, start, end)
                        exportStatus = "Exported $rowCount rows to CSV"
                    } catch (e: Exception) {
                        exportStatus = "Export failed: ${e.message}"
                    }
                    isExporting = false
                }
            }
        }
    }

    val jsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                scope.launch {
                    isExporting = true
                    try {
                        val (start, end) = getTimeRange()
                        rowCount = exportUseCase.exportToJson(context, uri, start, end)
                        exportStatus = "Exported $rowCount rows to JSON"
                    } catch (e: Exception) {
                        exportStatus = "Export failed: ${e.message}"
                    }
                    isExporting = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Data") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy,
                    titleContentColor = AccentCyan
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
            // Schema Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Export Schema", fontWeight = FontWeight.Bold, color = AccentCyan, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "timestamp, timestamp_readable, app_package, screen_id, event_type, dwell_ms",
                        color = AccentGreen,
                        fontSize = 12.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Event types: app_focus, tap, scroll, screen_change, self_report, reaction_test",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Format Selection
            Text("Format", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("CSV", "JSON").forEach { format ->
                    FilterChip(
                        selected = exportFormat == format,
                        onClick = { exportFormat = format },
                        label = { Text(format) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentCyan.copy(alpha = 0.2f),
                            selectedLabelColor = AccentCyan
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Time Range
            Text("Time Range", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("1h" to "1 Hour", "6h" to "6 Hours", "today" to "Today").forEach { (key, label) ->
                    FilterChip(
                        selected = timeRange == key,
                        onClick = { timeRange = key },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentCyan.copy(alpha = 0.2f),
                            selectedLabelColor = AccentCyan
                        )
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("7d" to "7 Days", "30d" to "30 Days", "all" to "All Data").forEach { (key, label) ->
                    FilterChip(
                        selected = timeRange == key,
                        onClick = { timeRange = key },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentCyan.copy(alpha = 0.2f),
                            selectedLabelColor = AccentCyan
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Export Button
            Button(
                onClick = {
                    val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                    val ext = if (exportFormat == "CSV") "csv" else "json"
                    val mimeType = if (exportFormat == "CSV") "text/csv" else "application/json"
                    val fileName = "dlt_export_${dateStr}.$ext"

                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = mimeType
                        putExtra(Intent.EXTRA_TITLE, fileName)
                    }

                    if (exportFormat == "CSV") {
                        csvLauncher.launch(intent)
                    } else {
                        jsonLauncher.launch(intent)
                    }
                },
                enabled = !isExporting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = DarkNavy,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Icon(Icons.Default.FileDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isExporting) "Exporting..." else "Export $exportFormat",
                    fontWeight = FontWeight.Bold
                )
            }

            // Status
            if (exportStatus.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (exportStatus.contains("failed")) AccentRed.copy(alpha = 0.1f)
                        else AccentGreen.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (exportStatus.contains("failed")) Icons.Default.Error
                            else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (exportStatus.contains("failed")) AccentRed else AccentGreen
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(exportStatus, color = TextPrimary, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
