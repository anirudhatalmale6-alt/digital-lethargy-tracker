package com.dld.tracker.ui.screen.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dld.tracker.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToSelfReport: () -> Unit,
    onNavigateToReactionTest: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("DL Tracker", fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tracking Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isTracking) MediumBlue else SurfaceCard
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (state.isTracking) "Tracking Active" else "Tracking Paused",
                            fontWeight = FontWeight.Bold,
                            color = if (state.isTracking) AccentGreen else AccentRed,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (state.isTracking) "Collecting usage data" else "Tap to start tracking",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = state.isTracking,
                        onCheckedChange = { viewModel.toggleTracking() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentGreen,
                            checkedTrackColor = AccentGreen.copy(alpha = 0.3f),
                            uncheckedThumbColor = AccentRed,
                            uncheckedTrackColor = AccentRed.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ACI Gauge
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "App Cognitive Inertia",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Last 30 minutes",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gauge
                    AciGauge(
                        score = state.aciResult.score,
                        level = state.aciResult.level,
                        modifier = Modifier.size(180.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ACI Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AciDetail("Dwell", "${state.aciResult.dwellConcentration}x")
                        AciDetail("Trans/min", "${state.aciResult.transitionRate}")
                        AciDetail("Int/min", "${state.aciResult.interactionDensity}")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = { viewModel.refreshAci() }) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Refresh", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Events",
                    value = "${state.totalEvents}",
                    color = AccentCyan
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Interactions",
                    value = "${state.totalInteractions}",
                    color = AccentGreen
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Sessions",
                    value = "${state.totalSessions}",
                    color = AccentOrange
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Apps Today",
                    value = "${state.todayApps}",
                    color = AccentYellow
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    label = "Dwell (min)",
                    value = "${state.todayDwellMinutes}",
                    color = AccentCyan
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Text(
                text = "Research Tools",
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            ActionButton(
                icon = Icons.Default.Edit,
                title = "Self-Report Label",
                subtitle = "Tag your current engagement state",
                color = AccentGreen,
                onClick = onNavigateToSelfReport
            )

            Spacer(modifier = Modifier.height(8.dp))

            ActionButton(
                icon = Icons.Default.Speed,
                title = "Reaction Time Test",
                subtitle = "Measure your cognitive response time",
                color = AccentOrange,
                onClick = onNavigateToReactionTest
            )

            Spacer(modifier = Modifier.height(8.dp))

            ActionButton(
                icon = Icons.Default.FileDownload,
                title = "Export Data",
                subtitle = "CSV or JSON export for your dataset",
                color = AccentCyan,
                onClick = onNavigateToExport
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AciGauge(score: Double, level: String, modifier: Modifier = Modifier) {
    val gaugeColor = when {
        score < 2.5 -> AccentGreen
        score < 5.0 -> AccentYellow
        score < 7.5 -> AccentOrange
        else -> AccentRed
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()
            val padding = strokeWidth / 2
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)

            // Background arc
            drawArc(
                color = TextMuted.copy(alpha = 0.3f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(padding, padding),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Score arc
            val sweep = (score / 10.0 * 270.0).toFloat()
            drawArc(
                color = gaugeColor,
                startAngle = 135f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(padding, padding),
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = gaugeColor
            )
            Text(
                text = level,
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun AciDetail(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
        Text(text = label, color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, label: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                Text(text = subtitle, color = TextSecondary, fontSize = 12.sp)
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted
            )
        }
    }
}
