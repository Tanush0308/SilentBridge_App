package com.silentbridge.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.silentbridge.domain.model.ConnectionState
import com.silentbridge.gesture.InferenceState
import com.silentbridge.presentation.viewmodel.MainViewModel
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextAlign


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToDevices: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToManageWords: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val languages = listOf(
        "en" to "English",
        "hi" to "Hindi (हिंदी)",
        "mr" to "Marathi (मराठी)",
        "gu" to "Gujarati (ગુજરાતી)",
        "ta" to "Tamil (தமிழ்)",
        "te" to "Telugu (తెలుగు)",
        "kn" to "Kannada (ಕನ್ನಡ)"
    )

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Output Language") },
            text = {
                Column {
                    languages.forEach { (code, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.targetLanguageCode == code,
                                onClick = {
                                    viewModel.setTargetLanguage(code)
                                    showLanguageDialog = false
                                }
                            )
                            Text(
                                text = name,
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text("Cancel") }
            }
        )
    }
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SilentBridge", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showLanguageDialog = true }) {
                        Icon(Icons.Default.Translate, contentDescription = "Language")
                    }
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Diagnostics") },
                            onClick = {
                                showMenu = false
                                onNavigateToDiagnostics()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Prediction Stats") },
                            onClick = {
                                showMenu = false
                                onNavigateToStats()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export Dataset (${uiState.feedbackCount})") },
                            onClick = {
                                showMenu = false
                                viewModel.exportDataset()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Manage Words") },
                            onClick = {
                                showMenu = false
                                onNavigateToManageWords()
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top: Connection Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                val color = when (uiState.connectionState) {
                    ConnectionState.CONNECTED -> Color(0xFF40a02b)
                    ConnectionState.CONNECTING -> Color(0xFFdf8e1d)
                    ConnectionState.DISCONNECTED, ConnectionState.ERROR -> Color(0xFFe64553)
                    ConnectionState.SEARCHING -> Color(0xFF1e66f5)
                }
                Surface(
                    modifier = Modifier.size(12.dp),
                    shape = CircleShape,
                    color = color
                ) {}
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Glove: ${uiState.connectionState.name}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.isDownloadingModel) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                Text("Downloading language pack...", style = MaterialTheme.typography.labelSmall)
            }

            if (uiState.connectionState != ConnectionState.CONNECTED) {
                Button(onClick = onNavigateToDevices, modifier = Modifier.fillMaxWidth()) {
                    Text("Connect Glove")
                }
            }

            // Center: Sentence Focal Point
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isFormingSentence) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "AI is translating...",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (uiState.sentenceError != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(Modifier.height(8.dp))
                            Text(text = uiState.sentenceError!!, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(onClick = { viewModel.clearSentence(); viewModel.clearBuffer() }) {
                                Text("Clear")
                            }
                        }
                    }
                } else if (uiState.formedSentence != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            if (uiState.translatedSentence != null && uiState.targetLanguageCode != "en") {
                                Text(
                                    text = uiState.translatedSentence!!,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 32.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            
                            Text(
                                text = uiState.formedSentence!!,
                                style = if (uiState.translatedSentence == null) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
                                fontWeight = if (uiState.translatedSentence == null) FontWeight.Bold else FontWeight.Normal,
                                color = if (uiState.translatedSentence == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Row(Modifier.padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Button(onClick = { viewModel.speakSentence(context) }, modifier = Modifier.weight(1f)) {
                                    Text("Speak", fontSize = 16.sp)
                                }
                                OutlinedButton(onClick = { viewModel.clearSentence(); viewModel.clearBuffer() }) {
                                    Text("Clear")
                                }
                            }
                        }
                    }
                } else {
                    // Empty State
                    Text(
                        text = "Ready to translate...",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }

            // Word Buffer (Just below center)
            if (uiState.wordBuffer.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .animateContentSize(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    itemsIndexed(uiState.wordBuffer) { index, word ->
                        AssistChip(
                            onClick = { viewModel.removeWordFromBuffer(index) },
                            label = { Text(word, fontSize = 16.sp) },
                            trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) },
                            modifier = Modifier.padding(horizontal = 4.dp),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom: Gesture Recognition Card & Controls
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (uiState.gestureResult != null) {
                        Text("Detected Gesture", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = uiState.gestureResult!!.gestureName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.isWrongFlash) Color.Red else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        if (uiState.showFeedbackButtons && !uiState.showCorrectionSelector) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 12.dp)) {
                                Button(onClick = { viewModel.onFeedbackYes() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF40a02b))) { Text("Correct") }
                                Button(onClick = { viewModel.onFeedbackNo() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFe64553))) { Text("Wrong") }
                            }
                        }
                    } else {
                        val statusText = when(uiState.inferenceState) {
                            InferenceState.READY -> "Ready - Press START"
                            InferenceState.CALIBRATING -> "Calibrating (Keep Still)..."
                            InferenceState.RECORDING -> "Recording Gesture..."
                            InferenceState.PREPROCESSING, InferenceState.MODEL_INFERENCE -> "Processing..."
                            InferenceState.DISCONNECTED -> "Connect Glove first"
                            else -> "Waiting..."
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Correction Selector
            if (uiState.showCorrectionSelector) {
                LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.height(150.dp).padding(top = 8.dp)) {
                    items(uiState.modelLabels + uiState.customLabels) { label ->
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.submitCorrectedLabel(label) },
                            label = { Text(label, fontSize = 12.sp) },
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }

            // Controls
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.inferenceState == InferenceState.READY) {
                    Button(
                        onClick = { viewModel.startGestureSession() },
                        enabled = uiState.connectionState == ConnectionState.CONNECTED,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("START", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                }
                
                if (viewModel.getTriggerModePublic() == "manual" && uiState.wordBuffer.isNotEmpty()) {
                    Button(
                        onClick = { viewModel.triggerSentenceFormation() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("DONE", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
