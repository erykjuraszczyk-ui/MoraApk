package pl.mora.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.mora.app.data.MemoryItem
import pl.mora.app.domain.ChatMessage
import pl.mora.app.integration.CompanionApp
import pl.mora.app.integration.DeviceDiagnosticsReport
import pl.mora.app.integration.PerformanceAssessment
import pl.mora.app.domain.EmotionResult
import java.text.DateFormat
import java.util.Date

private enum class AppTab(val title: String) {
    ASSISTANT("Asystent"),
    MEMORY("Pamięć"),
    SETTINGS("Ustawienia")
}

@Composable
fun MoraApp(
    viewModel: MoraViewModel,
    onVoiceClick: () -> Unit,
    onSpeak: (String) -> Unit,
    onStopSpeaking: () -> Unit,
    onShowOverlaySettings: () -> Unit,
    onStartOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
    companionApps: List<CompanionApp>,
    companionStatus: String,
    deviceReport: DeviceDiagnosticsReport?,
    onScanDevice: () -> Unit,
    performanceAssessment: PerformanceAssessment?,
    onAnalyzePerformance: () -> Unit,
    onApplyPerformanceProfile: (PerformanceAssessment) -> Unit,
    onScanCompanionApps: () -> Unit,
    onLaunchCompanionApp: (String) -> Unit,
    onTestNotification: () -> Unit
) {
    val state = viewModel.uiState
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.ASSISTANT) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFFF4D89),
            secondary = Color(0xFF79C8FF),
            background = Color(0xFF100D16),
            surface = Color(0xFF1D1724),
            onSurface = Color(0xFFF4EDF7)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(Modifier.fillMaxSize()) {
                when (selectedTab) {
                    AppTab.ASSISTANT -> AssistantScreen(
                        state = state,
                        viewModel = viewModel,
                        onVoiceClick = onVoiceClick,
                        onSpeak = onSpeak,
                        onStopSpeaking = onStopSpeaking
                    )
                    AppTab.MEMORY -> MemoryScreen(
                        memories = state.memories,
                        memoryEnabled = state.memoryEnabled,
                        onClear = viewModel::clearMemory
                    )
                    AppTab.SETTINGS -> SettingsScreen(
                        state = state,
                        viewModel = viewModel,
                        onShowOverlaySettings = onShowOverlaySettings,
                        onStartOverlay = onStartOverlay,
                        onStopOverlay = onStopOverlay,
                        companionApps = companionApps,
                        companionStatus = companionStatus,
                        deviceReport = deviceReport,
                        onScanDevice = onScanDevice,
                        performanceAssessment = performanceAssessment,
                        onAnalyzePerformance = onAnalyzePerformance,
                        onApplyPerformanceProfile = onApplyPerformanceProfile,
                        onScanCompanionApps = onScanCompanionApps,
                        onLaunchCompanionApp = onLaunchCompanionApp,
                        onTestNotification = onTestNotification
                    )
                }
                NavigationBar(
                    containerColor = Color(0xFF17121E),
                    windowInsets = WindowInsets.navigationBars
                ) {
                    AppTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        AppTab.ASSISTANT -> Icons.Default.Chat
                                        AppTab.MEMORY -> Icons.Default.Memory
                                        AppTab.SETTINGS -> Icons.Default.Settings
                                    },
                                    contentDescription = tab.title
                                )
                            },
                            label = { Text(tab.title) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssistantScreen(
    state: MoraUiState,
    viewModel: MoraViewModel,
    onVoiceClick: () -> Unit,
    onSpeak: (String) -> Unit,
    onStopSpeaking: () -> Unit
) {
    var input by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    if (state.cortanaMode) "CORTANA / MORA" else "MORA",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF78A6)
                )
                Text("REDMAGIC EDITION  •  ONLINE AI", fontSize = 10.sp, color = Color(0xFF9F93AA))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = !state.cortanaMode,
                    onClick = { viewModel.setCortanaMode(false) },
                    label = { Text("Mora") }
                )
                FilterChip(
                    selected = state.cortanaMode,
                    onClick = { viewModel.setCortanaMode(true) },
                    label = { Text("Cortana") }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MoraAvatar(
                emotion = state.emotion,
                cortanaMode = state.cortanaMode,
                reducedMotion = state.reducedMotion,
                modifier = Modifier.size(112.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    if (state.cortanaMode) "Tryb holograficzny Cortana" else "Mora jest gotowa",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Stan ekspresji: ${state.emotion.state.label}",
                    color = Color(state.emotion.state.argb),
                    fontSize = 13.sp
                )
                Text(
                    "Wskaźnik heurystyczny: ${(state.emotion.confidence * 100).toInt()}%",
                    color = Color(0xFFAAA1B5),
                    fontSize = 11.sp
                )
            }
        }

        Divider(color = Color(0xFF382B43))
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.messages, key = { it.id }) { message ->
                ChatBubble(message = message, onSpeak = onSpeak)
            }
            if (state.sending) {
                item {
                    Text("Mora przygotowuje odpowiedź…", color = Color(0xFF9F93AA), fontSize = 12.sp)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Napisz do Mory…") },
                maxLines = 3,
                shape = RoundedCornerShape(18.dp)
            )
            IconButton(
                onClick = onStopSpeaking,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Zatrzymaj czytanie", tint = Color(0xFF9CCFFF))
            }
            IconButton(
                onClick = onVoiceClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF392033), CircleShape)
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Rozpocznij rozmowę głosową", tint = Color(0xFFFF8DB5))
            }
            Button(
                onClick = {
                    viewModel.sendMessage(input)
                    input = ""
                },
                enabled = input.isNotBlank() && !state.sending,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Wyślij")
            }
        }
        Text(
            if (state.apiKeyConfigured && state.model.isNotBlank()) "Model skonfigurowany: ${state.model}" else "Skonfiguruj endpoint, model i klucz API w Ustawieniach.",
            color = Color(0xFF9F93AA),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ChatBubble(message: ChatMessage, onSpeak: (String) -> Unit) {
    val isAssistant = message.role == "assistant"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isAssistant) Arrangement.Start else Arrangement.End
    ) {
        Surface(
            color = if (isAssistant) Color(0xFF241B2C) else Color(0xFF422034),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isAssistant) "MORA" else "TY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAssistant) Color(0xFFFF86B0) else Color(0xFF8FD6FF)
                    )
                    if (isAssistant) {
                        IconButton(onClick = { onSpeak(message.content) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Odczytaj odpowiedź", tint = Color(0xFF9CCFFF))
                        }
                    }
                }
                Text(message.content, color = Color(0xFFF4EDF7), fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun MoraAvatar(
    emotion: EmotionResult,
    cortanaMode: Boolean,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "mora-hologram")
    val pulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val accent = if (cortanaMode) Color(0xFF72C8FF) else Color(emotion.state.argb)

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val center = this.center
            val radius = size.minDimension * 0.46f * if (reducedMotion) 1f else pulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.72f), accent.copy(alpha = 0.12f), Color.Transparent),
                    center = center,
                    radius = radius * 1.35f
                ),
                radius = radius * 1.35f,
                center = center
            )
            drawCircle(
                color = accent.copy(alpha = 0.75f),
                radius = radius * 0.82f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = accent.copy(alpha = 0.4f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = radius * 0.55f,
                center = center
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (cortanaMode) "C" else "M",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                if (cortanaMode) "CORTANA" else "MORA",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}

@Composable
private fun MemoryScreen(
    memories: List<MemoryItem>,
    memoryEnabled: Boolean,
    onClear: () -> Unit
) {
    var confirmClear by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Pamięć Mory", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(
            if (memoryEnabled) "Pamięć jest włączona. Nowe wpisy są szyfrowane lokalnie." else "Pamięć jest wyłączona. Włącz ją w Ustawieniach, aby zapisywać nowe wpisy.",
            color = Color(0xFFAAA1B5)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Zapisane wpisy: ${memories.size}")
            OutlinedButton(onClick = { confirmClear = true }, enabled = memories.isNotEmpty()) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(5.dp))
                Text("Usuń wszystko")
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(memories.reversed(), key = { it.id }) { item ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF211A29))) {
                    Column(Modifier.padding(12.dp)) {
                        Text(item.emotion, color = Color(0xFFFF91B9), fontWeight = FontWeight.SemiBold)
                        Text(item.text, color = Color(0xFFF4EDF7))
                        Text(
                            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(item.createdAt)),
                            color = Color(0xFF93899E),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Usunąć pamięć?") },
            text = { Text("Ta operacja usunie wszystkie lokalne wpisy zapisane przez Morę.") },
            confirmButton = {
                TextButton(onClick = { onClear(); confirmClear = false }) { Text("Usuń") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Anuluj") }
            }
        )
    }
}

@Composable
private fun SettingsScreen(
    state: MoraUiState,
    viewModel: MoraViewModel,
    onShowOverlaySettings: () -> Unit,
    onStartOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
    companionApps: List<CompanionApp>,
    companionStatus: String,
    deviceReport: DeviceDiagnosticsReport?,
    onScanDevice: () -> Unit,
    performanceAssessment: PerformanceAssessment?,
    onAnalyzePerformance: () -> Unit,
    onApplyPerformanceProfile: (PerformanceAssessment) -> Unit,
    onScanCompanionApps: () -> Unit,
    onLaunchCompanionApp: (String) -> Unit,
    onTestNotification: () -> Unit
) {
    var endpoint by rememberSaveable { mutableStateOf(state.endpoint) }
    var model by rememberSaveable { mutableStateOf(state.model) }
    var apiKey by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Ustawienia", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("AI Engine", color = Color(0xFFFF8DB5), fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = endpoint,
            onValueChange = { endpoint = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Endpoint HTTPS") },
            placeholder = { Text("https://api.openai.com/v1") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
        )
        OutlinedTextField(
            value = model,
            onValueChange = { model = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Model") },
            placeholder = { Text("Identyfikator modelu dostawcy") },
            singleLine = true
        )
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (state.apiKeyConfigured) "Nowy klucz API (puste = bez zmian)" else "Klucz API") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (state.apiKeyConfigured) "Klucz API jest zapisany w szyfrowanej postaci." else "Klucz API nie został jeszcze skonfigurowany.",
                modifier = Modifier.weight(1f),
                fontSize = 12.sp,
                color = Color(0xFFAAA1B5)
            )
            if (state.apiKeyConfigured) {
                TextButton(onClick = viewModel::clearApiKey) { Text("Usuń klucz") }
            }
        }
        Button(
            onClick = {
                viewModel.saveAiConfiguration(endpoint, model, apiKey)
                apiKey = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Zapisz konfigurację AI")
        }

        Divider(color = Color(0xFF382B43))
        Text("Pamięć i prywatność", color = Color(0xFFFF8DB5), fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Pamięć rozmów")
                Text("Nowe wpisy są zapisywane lokalnie po włączeniu tej opcji.", fontSize = 11.sp, color = Color(0xFFAAA1B5))
            }
            Switch(
                checked = state.memoryEnabled,
                onCheckedChange = viewModel::setMemoryEnabled
            )
        }

        Divider(color = Color(0xFF382B43))
        Text("REDMAGIC Integration", color = Color(0xFFFF8DB5), fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Tryb gry / ograniczone animacje")
                Text("Lokalny profil oszczędny Mory; nie steruje bezpośrednio Game Space.", fontSize = 11.sp, color = Color(0xFFAAA1B5))
            }
            Switch(
                checked = state.reducedMotion,
                onCheckedChange = viewModel::setReducedMotion
            )
        }
        OutlinedButton(onClick = onShowOverlaySettings, modifier = Modifier.fillMaxWidth()) {
            Text("Ustaw dostęp do pływającej nakładki")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onStartOverlay, modifier = Modifier.weight(1f)) { Text("Pokaż nakładkę") }
            OutlinedButton(onClick = onStopOverlay, modifier = Modifier.weight(1f)) { Text("Ukryj") }
        }
        OutlinedButton(onClick = onTestNotification, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Notifications, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Wyślij powiadomienie testowe")
        }

        Divider(color = Color(0xFF382B43))
        Text("Diagnostyka REDMAGIC 7", color = Color(0xFFFF8DB5), fontWeight = FontWeight.Bold)
        Text(
            "Odczyt lokalny na telefonie. Nie wymaga komputera ani debugowania USB.",
            fontSize = 11.sp,
            color = Color(0xFFAAA1B5)
        )
        OutlinedButton(
            onClick = onScanDevice,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Skanuj moje urządzenie")
        }
        Divider(color = Color(0xFF382B43))
        Text("REDMAGIC Performance Core 1.2.0", color = Color(0xFFFF8DB5), fontWeight = FontWeight.Bold)
        Text(
            "Analiza baterii, pamięci i stanu termicznego. MORA nie zmienia ustawień procesora ani systemowego trybu zasilania.",
            fontSize = 11.sp,
            color = Color(0xFFAAA1B5)
        )
        OutlinedButton(onClick = onAnalyzePerformance, modifier = Modifier.fillMaxWidth()) {
            Text("Analizuj wydajność telefonu")
        }
        performanceAssessment?.let { assessment ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF211A29)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Profil: ${assessment.profile.title}", fontWeight = FontWeight.Bold)
                    Text(assessment.explanation, fontSize = 12.sp, color = Color(0xFFAAA1B5))
                    Text(
                        "Bateria: ${assessment.batteryPercent?.let { "$it%" } ?: "brak danych"} · RAM dostępna: ${assessment.availableRamMb?.let { "$it MB" } ?: "brak danych"}",
                        fontSize = 11.sp
                    )
                    Text(
                        "Oszczędzanie energii: ${if (assessment.powerSaveMode) "włączone" else "wyłączone"} · Ograniczenie animacji zalecane: ${if (assessment.reducedMotionRecommended) "tak" else "nie"}",
                        fontSize = 11.sp
                    )
                    OutlinedButton(
                        onClick = { onApplyPerformanceProfile(assessment) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (assessment.reducedMotionRecommended) "Zastosuj ograniczone animacje" else "Zastosuj pełne animacje")
                    }
                }
            }
        }

        deviceReport?.let { report ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF211A29)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Raport urządzenia", fontWeight = FontWeight.Bold)
                    report.lines.forEach { (label, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = Color(0xFFAAA1B5), fontSize = 12.sp)
                            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    Text(
                        "Dane odczytane lokalnie: ${report.capturedAt}",
                        color = Color(0xFF93899E),
                        fontSize = 10.sp
                    )
                }
            }
        }

        Divider(color = Color(0xFF382B43))
        Text("Fabryczna Mora — Companion", color = Color(0xFFFF8DB5), fontWeight = FontWeight.Bold)
        Text(
            "Wyszukiwanie jest uruchamiane ręcznie. Oryginalna aplikacja pozostaje niezmieniona.",
            fontSize = 11.sp,
            color = Color(0xFFAAA1B5)
        )
        OutlinedButton(
            onClick = onScanCompanionApps,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Wyszukaj aplikacje Mora")
        }
        Text(companionStatus, fontSize = 12.sp, color = Color(0xFFAAA1B5))
        companionApps.forEach { app ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF211A29)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(app.displayName, fontWeight = FontWeight.SemiBold)
                        Text(app.packageName, fontSize = 10.sp, color = Color(0xFFAAA1B5))
                    }
                    OutlinedButton(onClick = { onLaunchCompanionApp(app.packageName) }) {
                        Text("Otwórz")
                    }
                }
            }
        }

        state.statusMessage?.let { message ->
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2034))) {
                Text(message, Modifier.padding(12.dp), color = Color(0xFFF4EDF7))
            }
        }
        Text(
            "Wskazówka: klucz API nie jest dołączany do APK ani repozytorium. Używaj wyłącznie zaufanego endpointu HTTPS.",
            fontSize = 11.sp,
            color = Color(0xFFAAA1B5)
        )
    }
}
