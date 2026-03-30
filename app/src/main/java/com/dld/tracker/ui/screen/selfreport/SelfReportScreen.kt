package com.dld.tracker.ui.screen.selfreport

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dld.tracker.DLTApplication
import com.dld.tracker.data.local.entity.SelfReportEntity
import com.dld.tracker.ui.theme.*
import kotlinx.coroutines.launch

data class LabelOption(
    val label: String,
    val description: String,
    val emoji: String
)

val LABEL_OPTIONS = listOf(
    LabelOption("bored", "Mindlessly scrolling without purpose", "😴"),
    LabelOption("engaged", "Actively interested in what I'm doing", "🎯"),
    LabelOption("distracted", "Jumping between apps without finishing anything", "🔀"),
    LabelOption("habitual", "Using apps out of habit, not need", "🔄"),
    LabelOption("purposeful", "Using device for a specific task", "✅"),
    LabelOption("anxious", "Checking notifications/apps compulsively", "😰"),
    LabelOption("procrastinating", "Avoiding something I should be doing", "⏰"),
    LabelOption("relaxing", "Deliberately unwinding with entertainment", "😌")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelfReportScreen(onBack: () -> Unit) {
    var selectedLabel by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Self-Report Label") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy,
                    titleContentColor = AccentGreen
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "How would you describe your current phone usage?",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (submitted) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MediumBlue)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = AccentGreen)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Label Recorded!", fontWeight = FontWeight.Bold, color = AccentGreen)
                            Text("Your response has been saved.", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        submitted = false
                        selectedLabel = null
                        notes = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                ) {
                    Text("Submit Another")
                }
            } else {
                LABEL_OPTIONS.forEach { option ->
                    val isSelected = selectedLabel == option.label
                    Card(
                        onClick = { selectedLabel = option.label },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MediumBlue else SurfaceCard
                        ),
                        border = if (isSelected) BorderStroke(2.dp, AccentGreen) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(option.emoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.label.replaceFirstChar { it.uppercase() },
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) AccentGreen else TextPrimary,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = option.description,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = AccentGreen)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Optional notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = TextMuted,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentCyan
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        selectedLabel?.let { label ->
                            scope.launch {
                                val app = DLTApplication.instance
                                val activeSession = app.usageRepository.getActiveSession()
                                app.usageRepository.insertSelfReport(
                                    SelfReportEntity(
                                        timestamp = System.currentTimeMillis(),
                                        label = label,
                                        sessionId = activeSession?.id,
                                        notes = notes.ifBlank { null }
                                    )
                                )
                                submitted = true
                            }
                        }
                    },
                    enabled = selectedLabel != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen,
                        disabledContainerColor = TextMuted
                    )
                ) {
                    Text("Submit Label", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
