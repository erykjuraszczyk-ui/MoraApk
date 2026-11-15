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
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.mora.app.domain.VoiceController
import pl.mora.app.integration.NotificationHelper
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
            val voice = remember { VoiceController(this@MainActivity) }

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
