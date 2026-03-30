package com.dld.tracker.ui.screen.reactiontest

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dld.tracker.DLTApplication
import com.dld.tracker.data.local.entity.ReactionTestEntity
import com.dld.tracker.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class TestPhase { IDLE, WAITING, READY, TOO_EARLY, RESULT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactionTestScreen(onBack: () -> Unit) {
    var phase by remember { mutableStateOf(TestPhase.IDLE) }
    var reactionTimeMs by remember { mutableLongStateOf(0L) }
    var readyTimestamp by remember { mutableLongStateOf(0L) }
    var testCount by remember { mutableIntStateOf(0) }
    var totalReactionMs by remember { mutableLongStateOf(0L) }
    val scope = rememberCoroutineScope()

    val circleColor = when (phase) {
        TestPhase.IDLE -> TextMuted
        TestPhase.WAITING -> AccentRed
        TestPhase.READY -> AccentGreen
        TestPhase.TOO_EARLY -> AccentOrange
        TestPhase.RESULT -> AccentCyan
    }

    val statusText = when (phase) {
        TestPhase.IDLE -> "Tap the circle to start"
        TestPhase.WAITING -> "Wait for green..."
        TestPhase.READY -> "TAP NOW!"
        TestPhase.TOO_EARLY -> "Too early! Tap to retry"
        TestPhase.RESULT -> "${reactionTimeMs} ms"
    }

    val subtitleText = when (phase) {
        TestPhase.IDLE -> "Measures cognitive response latency"
        TestPhase.WAITING -> "Don't tap until it turns green"
        TestPhase.READY -> "Tap as fast as you can!"
        TestPhase.TOO_EARLY -> "Wait for the circle to turn green"
        TestPhase.RESULT -> {
            val avg = if (testCount > 0) totalReactionMs / testCount else 0
            "Tests: $testCount | Average: ${avg}ms"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reaction Time Test") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy,
                    titleContentColor = AccentOrange
                )
            )
        },
        containerColor = DarkNavy
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = statusText,
                fontSize = if (phase == TestPhase.RESULT) 48.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                color = circleColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitleText,
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // The target circle
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(circleColor.copy(alpha = 0.2f))
                    .clickable {
                        when (phase) {
                            TestPhase.IDLE, TestPhase.TOO_EARLY, TestPhase.RESULT -> {
                                // Start a new test
                                phase = TestPhase.WAITING
                                scope.launch {
                                    val waitMs = (1000..4000).random().toLong()
                                    delay(waitMs)
                                    if (phase == TestPhase.WAITING) {
                                        phase = TestPhase.READY
                                        readyTimestamp = System.nanoTime()
                                    }
                                }
                            }
                            TestPhase.WAITING -> {
                                // Tapped too early
                                phase = TestPhase.TOO_EARLY
                            }
                            TestPhase.READY -> {
                                // Record reaction time
                                val elapsed = (System.nanoTime() - readyTimestamp) / 1_000_000
                                reactionTimeMs = elapsed
                                testCount++
                                totalReactionMs += elapsed
                                phase = TestPhase.RESULT

                                // Save to DB
                                scope.launch {
                                    val app = DLTApplication.instance
                                    val session = app.usageRepository.getActiveSession()
                                    app.usageRepository.insertReactionTest(
                                        ReactionTestEntity(
                                            timestamp = System.currentTimeMillis(),
                                            reactionTimeMs = elapsed,
                                            wasCorrect = true,
                                            sessionId = session?.id
                                        )
                                    )
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(circleColor.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(circleColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            if (testCount > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Session Summary", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$testCount", fontWeight = FontWeight.Bold, color = AccentCyan, fontSize = 20.sp)
                                Text("Tests", color = TextSecondary, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val avg = totalReactionMs / testCount
                                Text("${avg}ms", fontWeight = FontWeight.Bold, color = AccentGreen, fontSize = 20.sp)
                                Text("Average", color = TextSecondary, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${reactionTimeMs}ms", fontWeight = FontWeight.Bold, color = AccentOrange, fontSize = 20.sp)
                                Text("Last", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
