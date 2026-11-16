package pl.mora.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.mora.app.domain.VoiceController
import pl.mora.app.domain.VoiceUiData
import pl.mora.app.integration.CompanionApp
import pl.mora.app.integration.CompanionAppDiscovery
import pl.mora.app.integration.CompanionLaunchResult
import pl.mora.app.integration.DeviceDiagnostics
import pl.mora.app.integration.DeviceDiagnosticsReport
import pl.mora.app.integration.NotificationHelper
import pl.mora.app.integration.PerformanceCore
import pl.mora.app.integration.PerformanceAssessment
import pl.mora.app.integration.OverlayService
import pl.mora.app.ui.MoraApp
import pl.mora.app.ui.MoraViewModel
import pl.mora.app.ui.MoraViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: MoraViewModel = viewModel(
                factory = remember { MoraViewModelFactory(application) }
            )
            val companionDiscovery = remember { CompanionAppDiscovery(this@MainActivity) }
            val companionApps = remember { mutableStateOf(emptyList<CompanionApp>()) }
            val companionStatus = remember { mutableStateOf("Wyszukiwanie aplikacji fabrycznej jest opcjonalne.") }
            val deviceReport = remember { mutableStateOf<DeviceDiagnosticsReport?>(null) }
            val performanceAssessment = remember { mutableStateOf<PerformanceAssessment?>(null) }
            val voiceUiData = remember { mutableStateOf(VoiceUiData()) }
            val voice = remember {
                VoiceController(this@MainActivity) { voiceUiData.value = it }
            }

            DisposableEffect(voice) {
                onDispose { voice.release() }
            }

            val microphonePermission = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) {
                    val available = voice.startListening(viewModel::sendMessage)
                    if (!available) {
                        Toast.makeText(this@MainActivity, "Brak dostępnej usługi rozpoznawania mowy.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Mikrofon nie został udostępniony.", Toast.LENGTH_SHORT).show()
                }
            }

            val notificationPermission = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                if (granted) NotificationHelper.showTestNotification(this@MainActivity)
            }

            MoraApp(
                viewModel = viewModel,
                onVoiceClick = {
                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        val available = voice.startListening(viewModel::sendMessage)
                        if (!available) {
                            Toast.makeText(this@MainActivity, "Usługa rozpoznawania mowy nie jest dostępna.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onSpeak = voice::speak,
                onStopSpeaking = voice::stopSpeaking,
                voiceUiData = voiceUiData.value,
                onSpeechRateChange = { voiceUiData.value = voice.updateSpeechRate(it) },
                onSpeechPitchChange = { voiceUiData.value = voice.updatePitch(it) },
                onVoiceSelected = { voiceUiData.value = voice.selectVoice(it) },
                onRefreshVoiceOptions = { voiceUiData.value = voice.refreshVoiceOptions() },
                onPreviewVoice = { voice.speak("Cześć! Jestem Mora. Tak brzmi mój polski głos.") },
                onShowOverlaySettings = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                },
                onStartOverlay = {
                    if (Settings.canDrawOverlays(this@MainActivity)) {
                        startService(Intent(this@MainActivity, OverlayService::class.java))
                    } else {
                        Toast.makeText(this@MainActivity, "Najpierw przyznaj dostęp do nakładki.", Toast.LENGTH_SHORT).show()
                    }
                },
                onStopOverlay = {
                    stopService(Intent(this@MainActivity, OverlayService::class.java))
                },
                companionApps = companionApps.value,
                companionStatus = companionStatus.value,
                deviceReport = deviceReport.value,
                onScanDevice = { deviceReport.value = DeviceDiagnostics.inspect(this@MainActivity) },
                performanceAssessment = performanceAssessment.value,
                onAnalyzePerformance = { performanceAssessment.value = PerformanceCore.assess(this@MainActivity) },
                onApplyPerformanceProfile = { assessment -> viewModel.setReducedMotion(assessment.reducedMotionRecommended) },
                onScanCompanionApps = {
                    companionApps.value = companionDiscovery.findMoraApps()
                    companionStatus.value = if (companionApps.value.isEmpty()) {
                        "Nie znaleziono widocznej aplikacji z nazwą Mora. Asystent MORA AI działa niezależnie."
                    } else {
                        "Znaleziono aplikacji: ${companionApps.value.size}. Wybierz właściwą pozycję."
                    }
                },
                onLaunchCompanionApp = { packageName ->
                    companionStatus.value = when (val result = companionDiscovery.launch(packageName)) {
                        CompanionLaunchResult.Launched -> "Przekazano sterowanie do wybranej aplikacji."
                        CompanionLaunchResult.NotInstalledOrNotVisible -> "Aplikacja nie jest zainstalowana lub nie jest widoczna."
                        CompanionLaunchResult.NoLaunchActivity -> "Wybrana aplikacja nie udostępnia ekranu startowego."
                        is CompanionLaunchResult.Failed -> "Nie udało się uruchomić aplikacji: ${result.reason}"
                    }
                },
                onTestNotification = {
                    if (Build.VERSION.SDK_INT >= 33 &&
                        checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        NotificationHelper.showTestNotification(this@MainActivity)
                    }
                }
            )
        }
    }
}
