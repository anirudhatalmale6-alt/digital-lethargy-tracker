package com.dld.tracker.ui.screen.reactiontest

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dld.tracker.DLTApplication
import com.dld.tracker.data.local.entity.ReactionTestEntity
import com.dld.tracker.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class TestType(val title: String, val description: String) {
    SIMPLE("Simple Reaction", "Tap when the circle turns green"),
    CHOICE("Choice Reaction", "Tap the side matching the color"),
    GO_NOGO("Go / No-Go", "Tap for green, hold for red"),
    SEQUENCE("Sequence Memory", "Remember and repeat the pattern"),
    SUSTAINED("Sustained Attention", "Tap only when you see the target")
}

enum class TestPhase { MENU, IDLE, WAITING, READY, TOO_EARLY, RESULT, SEQUENCE_SHOW, SEQUENCE_INPUT, SUSTAINED_ACTIVE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactionTestScreen(onBack: () -> Unit) {
    var currentTest by remember { mutableStateOf<TestType?>(null) }
    var phase by remember { mutableStateOf(TestPhase.MENU) }
    var reactionTimeMs by remember { mutableLongStateOf(0L) }
    var readyTimestamp by remember { mutableLongStateOf(0L) }
    var testCount by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var totalReactionMs by remember { mutableLongStateOf(0L) }
    var statusMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Choice test state
    var choiceIsLeft by remember { mutableStateOf(true) }

    // Go/No-Go state
    var isGoTrial by remember { mutableStateOf(true) }
    var noGoErrors by remember { mutableIntStateOf(0) }

    // Sequence memory state
    var sequence by remember { mutableStateOf(listOf<Int>()) }
    var playerInput by remember { mutableStateOf(listOf<Int>()) }
    var sequenceLevel by remember { mutableIntStateOf(3) }
    var showingIndex by remember { mutableIntStateOf(-1) }

    // Sustained attention state
    var sustainedTarget by remember { mutableStateOf("X") }
    var sustainedCurrent by remember { mutableStateOf("") }
    var sustainedRound by remember { mutableIntStateOf(0) }
    var sustainedTotal by remember { mutableIntStateOf(15) }
    var sustainedMisses by remember { mutableIntStateOf(0) }
    var sustainedFalseAlarms by remember { mutableIntStateOf(0) }

    fun resetTest() {
        testCount = 0
        correctCount = 0
        totalReactionMs = 0
        noGoErrors = 0
        sequenceLevel = 3
        sustainedRound = 0
        sustainedMisses = 0
        sustainedFalseAlarms = 0
        statusMessage = ""
    }

    fun saveResult(reactionMs: Long, wasCorrect: Boolean) {
        scope.launch {
            val app = DLTApplication.instance
            val session = app.usageRepository.getActiveSession()
            app.usageRepository.insertReactionTest(
                ReactionTestEntity(
                    timestamp = System.currentTimeMillis(),
                    reactionTimeMs = reactionMs,
                    wasCorrect = wasCorrect,
                    sessionId = session?.id
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTest?.title ?: "Reaction Tests") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (phase == TestPhase.MENU) onBack()
                        else { phase = TestPhase.MENU; currentTest = null; resetTest() }
                    }) {
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (phase) {
                TestPhase.MENU -> {
                    // Test Selection Menu
                    Text(
                        text = "Choose a Cognitive Test",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TestType.entries.forEachIndexed { index, test ->
                            val colors = listOf(AccentGreen, AccentCyan, AccentOrange, AccentYellow, AccentRed)
                            Card(
                                onClick = {
                                    currentTest = test
                                    phase = TestPhase.IDLE
                                    resetTest()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(colors[index].copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            fontWeight = FontWeight.Bold,
                                            color = colors[index],
                                            fontSize = 20.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(test.title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                                        Text(test.description, color = TextSecondary, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("About These Tests", fontWeight = FontWeight.Bold, color = AccentCyan, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "These cognitive tests measure different aspects of mental performance that correlate with digital lethargy:\n\n" +
                                    "- Simple Reaction: basic processing speed\n" +
                                    "- Choice Reaction: decision-making speed\n" +
                                    "- Go/No-Go: impulse control\n" +
                                    "- Sequence Memory: working memory capacity\n" +
                                    "- Sustained Attention: focus/vigilance over time",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                else -> {
                    // Test Execution Area
                    when (currentTest) {
                        TestType.SIMPLE -> SimpleReactionTest(
                            phase = phase,
                            reactionTimeMs = reactionTimeMs,
                            testCount = testCount,
                            totalReactionMs = totalReactionMs,
                            statusMessage = statusMessage,
                            onPhaseChange = { phase = it },
                            onStart = {
                                phase = TestPhase.WAITING
                                scope.launch {
                                    delay((1000..4000).random().toLong())
                                    if (phase == TestPhase.WAITING) {
                                        phase = TestPhase.READY
                                        readyTimestamp = System.nanoTime()
                                    }
                                }
                            },
                            onTap = {
                                when (phase) {
                                    TestPhase.WAITING -> {
                                        phase = TestPhase.TOO_EARLY
                                        statusMessage = "Too early!"
                                    }
                                    TestPhase.READY -> {
                                        val elapsed = (System.nanoTime() - readyTimestamp) / 1_000_000
                                        reactionTimeMs = elapsed
                                        testCount++
                                        correctCount++
                                        totalReactionMs += elapsed
                                        phase = TestPhase.RESULT
                                        statusMessage = "${elapsed}ms"
                                        saveResult(elapsed, true)
                                    }
                                    else -> {}
                                }
                            }
                        )

                        TestType.CHOICE -> ChoiceReactionTest(
                            phase = phase,
                            reactionTimeMs = reactionTimeMs,
                            testCount = testCount,
                            correctCount = correctCount,
                            totalReactionMs = totalReactionMs,
                            isLeft = choiceIsLeft,
                            onStart = {
                                phase = TestPhase.WAITING
                                scope.launch {
                                    delay((1000..3000).random().toLong())
                                    if (phase == TestPhase.WAITING) {
                                        choiceIsLeft = (0..1).random() == 0
                                        phase = TestPhase.READY
                                        readyTimestamp = System.nanoTime()
                                    }
                                }
                            },
                            onChoose = { tappedLeft ->
                                if (phase == TestPhase.READY) {
                                    val elapsed = (System.nanoTime() - readyTimestamp) / 1_000_000
                                    reactionTimeMs = elapsed
                                    testCount++
                                    totalReactionMs += elapsed
                                    val correct = tappedLeft == choiceIsLeft
                                    if (correct) correctCount++
                                    phase = TestPhase.RESULT
                                    statusMessage = if (correct) "${elapsed}ms - Correct!" else "${elapsed}ms - Wrong side!"
                                    saveResult(elapsed, correct)
                                } else if (phase == TestPhase.WAITING) {
                                    phase = TestPhase.TOO_EARLY
                                    statusMessage = "Too early!"
                                }
                            },
                            onPhaseChange = { phase = it },
                            statusMessage = statusMessage
                        )

                        TestType.GO_NOGO -> GoNoGoTest(
                            phase = phase,
                            reactionTimeMs = reactionTimeMs,
                            testCount = testCount,
                            correctCount = correctCount,
                            noGoErrors = noGoErrors,
                            totalReactionMs = totalReactionMs,
                            isGoTrial = isGoTrial,
                            statusMessage = statusMessage,
                            onStart = {
                                phase = TestPhase.WAITING
                                scope.launch {
                                    delay((1000..3000).random().toLong())
                                    if (phase == TestPhase.WAITING) {
                                        isGoTrial = (0..2).random() != 0 // 66% go, 33% no-go
                                        phase = TestPhase.READY
                                        readyTimestamp = System.nanoTime()
                                        // Auto-advance after 1.5s for no-go trials
                                        delay(1500)
                                        if (phase == TestPhase.READY) {
                                            testCount++
                                            if (!isGoTrial) {
                                                correctCount++ // Correctly withheld
                                                statusMessage = "Correct! You held back."
                                            } else {
                                                statusMessage = "Too slow! You missed the green."
                                            }
                                            phase = TestPhase.RESULT
                                            saveResult(1500, !isGoTrial)
                                        }
                                    }
                                }
                            },
                            onTap = {
                                if (phase == TestPhase.READY) {
                                    val elapsed = (System.nanoTime() - readyTimestamp) / 1_000_000
                                    reactionTimeMs = elapsed
                                    testCount++
                                    totalReactionMs += elapsed
                                    if (isGoTrial) {
                                        correctCount++
                                        statusMessage = "${elapsed}ms - Correct tap!"
                                        saveResult(elapsed, true)
                                    } else {
                                        noGoErrors++
                                        statusMessage = "${elapsed}ms - Should have held! (Red = don't tap)"
                                        saveResult(elapsed, false)
                                    }
                                    phase = TestPhase.RESULT
                                } else if (phase == TestPhase.WAITING) {
                                    phase = TestPhase.TOO_EARLY
                                    statusMessage = "Too early! Wait for a color."
                                }
                            },
                            onPhaseChange = { phase = it }
                        )

                        TestType.SEQUENCE -> SequenceMemoryTest(
                            phase = phase,
                            sequenceLevel = sequenceLevel,
                            showingIndex = showingIndex,
                            sequence = sequence,
                            playerInput = playerInput,
                            testCount = testCount,
                            correctCount = correctCount,
                            statusMessage = statusMessage,
                            onStart = {
                                val newSeq = (1..sequenceLevel).map { (0..3).random() }
                                sequence = newSeq
                                playerInput = emptyList()
                                phase = TestPhase.SEQUENCE_SHOW
                                scope.launch {
                                    delay(500)
                                    for (i in newSeq.indices) {
                                        showingIndex = i
                                        delay(600)
                                        showingIndex = -1
                                        delay(200)
                                    }
                                    showingIndex = -1
                                    phase = TestPhase.SEQUENCE_INPUT
                                }
                            },
                            onTapPad = { padIndex ->
                                if (phase == TestPhase.SEQUENCE_INPUT) {
                                    val newInput = playerInput + padIndex
                                    playerInput = newInput
                                    val pos = newInput.size - 1
                                    if (newInput[pos] != sequence[pos]) {
                                        // Wrong
                                        testCount++
                                        phase = TestPhase.RESULT
                                        statusMessage = "Wrong! Reached level $sequenceLevel"
                                        sequenceLevel = 3
                                        saveResult(sequenceLevel.toLong(), false)
                                    } else if (newInput.size == sequence.size) {
                                        // Completed sequence
                                        testCount++
                                        correctCount++
                                        sequenceLevel++
                                        phase = TestPhase.RESULT
                                        statusMessage = "Correct! Level $sequenceLevel next"
                                        saveResult(sequenceLevel.toLong(), true)
                                    }
                                }
                            },
                            onPhaseChange = { phase = it }
                        )

                        TestType.SUSTAINED -> SustainedAttentionTest(
                            phase = phase,
                            sustainedCurrent = sustainedCurrent,
                            sustainedRound = sustainedRound,
                            sustainedTotal = sustainedTotal,
                            sustainedMisses = sustainedMisses,
                            sustainedFalseAlarms = sustainedFalseAlarms,
                            testCount = testCount,
                            correctCount = correctCount,
                            statusMessage = statusMessage,
                            onStart = {
                                sustainedRound = 0
                                sustainedMisses = 0
                                sustainedFalseAlarms = 0
                                phase = TestPhase.SUSTAINED_ACTIVE
                                scope.launch {
                                    val letters = listOf("X", "O", "A", "B", "X", "C", "X", "D", "O", "X", "E", "F", "X", "G", "O")
                                    for (i in letters.indices) {
                                        sustainedCurrent = letters[i]
                                        sustainedRound = i + 1
                                        readyTimestamp = System.nanoTime()
                                        delay(1200)
                                        if (phase != TestPhase.SUSTAINED_ACTIVE) break
                                        // If it was target and user didn't tap
                                        if (sustainedCurrent == "X" && phase == TestPhase.SUSTAINED_ACTIVE) {
                                            sustainedMisses++
                                        }
                                        sustainedCurrent = ""
                                        delay(300)
                                    }
                                    if (phase == TestPhase.SUSTAINED_ACTIVE) {
                                        testCount++
                                        val accuracy = if (sustainedTotal > 0) {
                                            ((sustainedTotal - sustainedMisses - sustainedFalseAlarms).coerceAtLeast(0) * 100) / sustainedTotal
                                        } else 0
                                        statusMessage = "Done! Accuracy: $accuracy%"
                                        phase = TestPhase.RESULT
                                        saveResult(accuracy.toLong(), accuracy > 70)
                                    }
                                }
                            },
                            onTap = {
                                if (phase == TestPhase.SUSTAINED_ACTIVE && sustainedCurrent.isNotEmpty()) {
                                    if (sustainedCurrent == "X") {
                                        correctCount++
                                        sustainedCurrent = "HIT"
                                    } else {
                                        sustainedFalseAlarms++
                                        sustainedCurrent = "MISS"
                                    }
                                }
                            },
                            onPhaseChange = { phase = it }
                        )

                        null -> { phase = TestPhase.MENU }
                    }
                }
            }
        }
    }
}

// ---- SIMPLE REACTION TEST ----
@Composable
fun SimpleReactionTest(
    phase: TestPhase, reactionTimeMs: Long, testCount: Int, totalReactionMs: Long,
    statusMessage: String, onPhaseChange: (TestPhase) -> Unit, onStart: () -> Unit, onTap: () -> Unit
) {
    val color = when (phase) {
        TestPhase.IDLE, TestPhase.TOO_EARLY -> TextMuted
        TestPhase.WAITING -> AccentRed
        TestPhase.READY -> AccentGreen
        TestPhase.RESULT -> AccentCyan
        else -> TextMuted
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (phase) {
                TestPhase.IDLE -> "Tap the circle to start"
                TestPhase.WAITING -> "Wait for green..."
                TestPhase.READY -> "TAP NOW!"
                TestPhase.TOO_EARLY -> "Too early! Tap to retry"
                TestPhase.RESULT -> statusMessage
                else -> ""
            },
            fontSize = if (phase == TestPhase.RESULT) 42.sp else 22.sp,
            fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        TargetCircle(color = color, size = 180, onClick = {
            when (phase) {
                TestPhase.IDLE, TestPhase.TOO_EARLY, TestPhase.RESULT -> onStart()
                TestPhase.WAITING, TestPhase.READY -> onTap()
                else -> {}
            }
        })
        Spacer(modifier = Modifier.height(32.dp))
        if (testCount > 0) TestStats(testCount, totalReactionMs / testCount, reactionTimeMs)
    }
}

// ---- CHOICE REACTION TEST ----
@Composable
fun ChoiceReactionTest(
    phase: TestPhase, reactionTimeMs: Long, testCount: Int, correctCount: Int,
    totalReactionMs: Long, isLeft: Boolean, statusMessage: String,
    onStart: () -> Unit, onChoose: (Boolean) -> Unit, onPhaseChange: (TestPhase) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (phase) {
                TestPhase.IDLE -> "Tap the side where the color appears"
                TestPhase.WAITING -> "Wait..."
                TestPhase.READY -> "Which side?"
                TestPhase.TOO_EARLY -> "Too early!"
                TestPhase.RESULT -> statusMessage
                else -> ""
            },
            fontSize = 20.sp, fontWeight = FontWeight.Bold,
            color = if (phase == TestPhase.RESULT && statusMessage.contains("Correct")) AccentGreen else TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val leftColor = when {
                phase == TestPhase.READY && isLeft -> AccentGreen
                phase == TestPhase.WAITING -> AccentRed.copy(alpha = 0.3f)
                else -> TextMuted.copy(alpha = 0.3f)
            }
            val rightColor = when {
                phase == TestPhase.READY && !isLeft -> AccentCyan
                phase == TestPhase.WAITING -> AccentRed.copy(alpha = 0.3f)
                else -> TextMuted.copy(alpha = 0.3f)
            }

            TargetCircle(color = leftColor, size = 130, onClick = {
                when (phase) {
                    TestPhase.IDLE, TestPhase.TOO_EARLY, TestPhase.RESULT -> onStart()
                    TestPhase.WAITING, TestPhase.READY -> onChoose(true)
                    else -> {}
                }
            })
            TargetCircle(color = rightColor, size = 130, onClick = {
                when (phase) {
                    TestPhase.IDLE, TestPhase.TOO_EARLY, TestPhase.RESULT -> onStart()
                    TestPhase.WAITING, TestPhase.READY -> onChoose(false)
                    else -> {}
                }
            })
        }

        Spacer(modifier = Modifier.height(32.dp))
        if (testCount > 0) {
            TestStats(testCount, if (correctCount > 0) totalReactionMs / correctCount else 0, reactionTimeMs, correctCount)
        }
    }
}

// ---- GO / NO-GO TEST ----
@Composable
fun GoNoGoTest(
    phase: TestPhase, reactionTimeMs: Long, testCount: Int, correctCount: Int,
    noGoErrors: Int, totalReactionMs: Long, isGoTrial: Boolean, statusMessage: String,
    onStart: () -> Unit, onTap: () -> Unit, onPhaseChange: (TestPhase) -> Unit
) {
    val color = when {
        phase == TestPhase.READY && isGoTrial -> AccentGreen
        phase == TestPhase.READY && !isGoTrial -> AccentRed
        phase == TestPhase.WAITING -> AccentYellow
        else -> TextMuted
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (phase) {
                TestPhase.IDLE -> "Green = TAP, Red = HOLD"
                TestPhase.WAITING -> "Get ready..."
                TestPhase.READY -> if (isGoTrial) "GO! TAP!" else "HOLD! Don't tap!"
                TestPhase.TOO_EARLY -> statusMessage
                TestPhase.RESULT -> statusMessage
                else -> ""
            },
            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        TargetCircle(color = color, size = 180, onClick = {
            when (phase) {
                TestPhase.IDLE, TestPhase.TOO_EARLY, TestPhase.RESULT -> onStart()
                TestPhase.WAITING, TestPhase.READY -> onTap()
                else -> {}
            }
        })
        Spacer(modifier = Modifier.height(32.dp))
        if (testCount > 0) {
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceCard), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem("Trials", "$testCount", AccentCyan)
                    StatItem("Correct", "$correctCount", AccentGreen)
                    StatItem("Impulse Errors", "$noGoErrors", AccentRed)
                }
            }
        }
    }
}

// ---- SEQUENCE MEMORY TEST ----
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SequenceMemoryTest(
    phase: TestPhase, sequenceLevel: Int, showingIndex: Int, sequence: List<Int>, playerInput: List<Int>,
    testCount: Int, correctCount: Int, statusMessage: String,
    onStart: () -> Unit, onTapPad: (Int) -> Unit, onPhaseChange: (TestPhase) -> Unit
) {
    val padColors = listOf(AccentRed, AccentGreen, AccentCyan, AccentYellow)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (phase) {
                TestPhase.IDLE -> "Watch the pattern, then repeat it"
                TestPhase.SEQUENCE_SHOW -> "Watch... (Level $sequenceLevel)"
                TestPhase.SEQUENCE_INPUT -> "Your turn! (${playerInput.size}/$sequenceLevel)"
                TestPhase.RESULT -> statusMessage
                else -> ""
            },
            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AccentYellow, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        // 2x2 pad grid
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (row in 0..1) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (col in 0..1) {
                        val padIndex = row * 2 + col
                        val isHighlighted = phase == TestPhase.SEQUENCE_SHOW && showingIndex >= 0 &&
                                showingIndex < sequence.size && sequence[showingIndex] == padIndex
                        val baseAlpha = if (phase == TestPhase.SEQUENCE_INPUT || phase == TestPhase.IDLE || phase == TestPhase.RESULT) 0.4f else 0.15f
                        val padAlpha = if (isHighlighted) 1.0f else baseAlpha

                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(padColors[padIndex].copy(alpha = padAlpha))
                                .clickable(enabled = phase == TestPhase.SEQUENCE_INPUT || phase == TestPhase.IDLE || phase == TestPhase.RESULT) {
                                    if (phase == TestPhase.IDLE || phase == TestPhase.RESULT) onStart()
                                    else onTapPad(padIndex)
                                }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        if (phase == TestPhase.IDLE || phase == TestPhase.RESULT) {
            Button(onClick = onStart, colors = ButtonDefaults.buttonColors(containerColor = AccentYellow)) {
                Text(if (phase == TestPhase.IDLE) "Start" else "Next Round", fontWeight = FontWeight.Bold, color = DarkNavy)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        if (testCount > 0) {
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceCard), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem("Rounds", "$testCount", AccentCyan)
                    StatItem("Correct", "$correctCount", AccentGreen)
                    StatItem("Level", "$sequenceLevel", AccentYellow)
                }
            }
        }
    }
}

// ---- SUSTAINED ATTENTION TEST ----
@Composable
fun SustainedAttentionTest(
    phase: TestPhase, sustainedCurrent: String, sustainedRound: Int, sustainedTotal: Int,
    sustainedMisses: Int, sustainedFalseAlarms: Int, testCount: Int, correctCount: Int,
    statusMessage: String, onStart: () -> Unit, onTap: () -> Unit, onPhaseChange: (TestPhase) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (phase) {
                TestPhase.IDLE -> "Tap ONLY when you see 'X'"
                TestPhase.SUSTAINED_ACTIVE -> "Tap when you see X ($sustainedRound/$sustainedTotal)"
                TestPhase.RESULT -> statusMessage
                else -> ""
            },
            fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AccentCyan, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))

        if (phase == TestPhase.SUSTAINED_ACTIVE) {
            val displayColor = when (sustainedCurrent) {
                "X" -> AccentGreen
                "HIT" -> AccentCyan
                "MISS" -> AccentRed
                "" -> TextMuted.copy(alpha = 0.1f)
                else -> AccentOrange
            }
            val displayText = when (sustainedCurrent) {
                "HIT" -> "O"
                "MISS" -> "X"
                else -> sustainedCurrent
            }

            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(displayColor.copy(alpha = 0.2f))
                    .clickable { onTap() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayText,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = displayColor
                )
            }
        } else {
            TargetCircle(color = if (phase == TestPhase.RESULT) AccentCyan else TextMuted, size = 180, onClick = {
                if (phase == TestPhase.IDLE || phase == TestPhase.RESULT) onStart()
            })
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (phase == TestPhase.IDLE) {
            Button(onClick = onStart, colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)) {
                Text("Start Test", fontWeight = FontWeight.Bold, color = DarkNavy)
            }
        }

        if (phase == TestPhase.RESULT || (phase == TestPhase.SUSTAINED_ACTIVE && sustainedRound > 0)) {
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceCard), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem("Hits", "$correctCount", AccentGreen)
                    StatItem("Misses", "$sustainedMisses", AccentOrange)
                    StatItem("False Alarms", "$sustainedFalseAlarms", AccentRed)
                }
            }
        }

        if (phase == TestPhase.RESULT) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onStart, colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)) {
                Text("Try Again", fontWeight = FontWeight.Bold, color = DarkNavy)
            }
        }
    }
}

// ---- SHARED COMPONENTS ----
@Composable
fun TargetCircle(color: Color, size: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size((size * 0.8).toInt().dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size((size * 0.6).toInt().dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun TestStats(testCount: Int, avgMs: Long, lastMs: Long, correctCount: Int? = null) {
    Card(colors = CardDefaults.cardColors(containerColor = SurfaceCard), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatItem("Tests", "$testCount", AccentCyan)
            StatItem("Avg", "${avgMs}ms", AccentGreen)
            StatItem("Last", "${lastMs}ms", AccentOrange)
            if (correctCount != null) {
                StatItem("Correct", "$correctCount", AccentYellow)
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 18.sp)
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}
