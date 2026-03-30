package com.dld.tracker.ui.screen.privacy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dld.tracker.ui.theme.*

@Composable
fun PrivacyDisclosureScreen(onAccepted: () -> Unit) {
    var agreed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Digital Lethargy\nTracker",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = AccentCyan,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Research Data Collection Tool",
            fontSize = 14.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "What We Collect",
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                val collected = listOf(
                    "App package names & usage duration",
                    "Screen transition events (which Activity is shown)",
                    "Interaction counts (tap, scroll) — type only, not content",
                    "Timestamps of all events",
                    "Self-reported labels you choose to tag",
                    "Reaction time test results"
                )
                collected.forEach { item ->
                    Text(
                        text = "• $item",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "What We DO NOT Collect",
                    fontWeight = FontWeight.Bold,
                    color = AccentRed,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                val notCollected = listOf(
                    "Text you type or read",
                    "Passwords or login credentials",
                    "Photos, messages, or personal content",
                    "Contact lists or call logs",
                    "Location data or GPS coordinates",
                    "Any personally identifiable information"
                )
                notCollected.forEach { item ->
                    Text(
                        text = "• $item",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Data Storage",
                    fontWeight = FontWeight.Bold,
                    color = AccentOrange,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All data is stored locally on your device in an encrypted database (SQLCipher). " +
                            "Data is only shared if you explicitly export it or enable REST sync to a research endpoint. " +
                            "You can pause tracking, export, or delete all data at any time from Settings.",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = agreed,
                onCheckedChange = { agreed = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = AccentCyan,
                    uncheckedColor = TextMuted
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "I understand what data is collected and consent to participate in this research study",
                color = TextPrimary,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onAccepted,
            enabled = agreed,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentCyan,
                disabledContainerColor = TextMuted
            )
        ) {
            Text(
                text = "Accept & Continue",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
