package com.dld.tracker.domain.usecase

import android.content.Context
import android.net.Uri
import com.dld.tracker.data.repository.UsageRepository
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

class ExportDataUseCase(private val repository: UsageRepository) {

    data class ExportRow(
        val timestamp: Long,
        val appPackage: String,
        val screenId: String?,
        val eventType: String,
        val dwellMs: Long
    )

    suspend fun getExportData(startMs: Long, endMs: Long): List<ExportRow> {
        val rows = mutableListOf<ExportRow>()

        // Usage events (app focus periods)
        val usageEvents = repository.getUsageEventsByRange(startMs, endMs)
        for (event in usageEvents) {
            rows.add(
                ExportRow(
                    timestamp = event.timestamp,
                    appPackage = event.appPackage,
                    screenId = null,
                    eventType = "app_focus",
                    dwellMs = event.dwellMs
                )
            )
        }

        // Interaction events (taps, scrolls, screen changes)
        val interactions = repository.getInteractionsByRange(startMs, endMs)
        for (event in interactions) {
            rows.add(
                ExportRow(
                    timestamp = event.timestamp,
                    appPackage = event.appPackage,
                    screenId = event.screenId,
                    eventType = event.eventType,
                    dwellMs = 0
                )
            )
        }

        // Self reports
        val selfReports = repository.getSelfReportsByRange(startMs, endMs)
        for (report in selfReports) {
            rows.add(
                ExportRow(
                    timestamp = report.timestamp,
                    appPackage = "self_report",
                    screenId = report.label,
                    eventType = "self_report",
                    dwellMs = 0
                )
            )
        }

        // Reaction tests
        val reactionTests = repository.getReactionTestsByRange(startMs, endMs)
        for (test in reactionTests) {
            rows.add(
                ExportRow(
                    timestamp = test.timestamp,
                    appPackage = "reaction_test",
                    screenId = if (test.wasCorrect) "correct" else "incorrect",
                    eventType = "reaction_test",
                    dwellMs = test.reactionTimeMs
                )
            )
        }

        return rows.sortedBy { it.timestamp }
    }

    suspend fun exportToCsv(context: Context, uri: Uri, startMs: Long, endMs: Long): Int {
        val rows = getExportData(startMs, endMs)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            val writer = OutputStreamWriter(outputStream)
            writer.write("timestamp,timestamp_readable,app_package,screen_id,event_type,dwell_ms\n")
            for (row in rows) {
                val readable = dateFormat.format(Date(row.timestamp))
                val screenId = row.screenId?.replace(",", ";") ?: ""
                writer.write("${row.timestamp},${readable},${row.appPackage},${screenId},${row.eventType},${row.dwellMs}\n")
            }
            writer.flush()
        }

        return rows.size
    }

    suspend fun exportToJson(context: Context, uri: Uri, startMs: Long, endMs: Long): Int {
        val rows = getExportData(startMs, endMs)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            val writer = OutputStreamWriter(outputStream)
            writer.write("[\n")
            rows.forEachIndexed { index, row ->
                val json = buildString {
                    append("  {")
                    append("\"timestamp\":${row.timestamp},")
                    append("\"timestamp_readable\":\"${dateFormat.format(Date(row.timestamp))}\",")
                    append("\"app_package\":\"${row.appPackage}\",")
                    append("\"screen_id\":\"${row.screenId ?: ""}\",")
                    append("\"event_type\":\"${row.eventType}\",")
                    append("\"dwell_ms\":${row.dwellMs}")
                    append("}")
                }
                writer.write(json)
                if (index < rows.size - 1) writer.write(",")
                writer.write("\n")
            }
            writer.write("]\n")
            writer.flush()
        }

        return rows.size
    }
}
