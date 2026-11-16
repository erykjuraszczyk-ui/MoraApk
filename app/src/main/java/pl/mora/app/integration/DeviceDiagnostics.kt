package pl.mora.app.integration

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.app.ActivityManager
import android.hardware.display.DisplayManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A small, privacy-conscious device report created entirely on the phone.
 *
 * It reads public Android APIs only. It does not read serial numbers,
 * account identifiers, installed-app inventories, or private vendor APIs.
 */
data class DeviceDiagnosticsReport(
    val lines: List<Pair<String, String>>,
    val capturedAt: String
)

object DeviceDiagnostics {

    fun inspect(context: Context): DeviceDiagnosticsReport {
        val appContext = context.applicationContext
        val activityManager = appContext.getSystemService(ActivityManager::class.java)
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)

        val batteryIntent = appContext.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val batteryLevel = batteryIntent?.let { intent ->
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) (level * 100f / scale).toInt() else null
        }

        val batteryStatus = batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_STATUS,
            -1
        ) ?: -1

        val chargingLabel = when (batteryStatus) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Ładowanie"
            BatteryManager.BATTERY_STATUS_FULL -> "Pełna"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Rozładowywanie"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Nie ładuje"
            else -> "Nieznany"
        }

        val batteryTemperature = batteryIntent
            ?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
            ?.takeIf { it >= 0 }
            ?.let { String.format(Locale.getDefault(), "%.1f °C", it / 10f) }
            ?: "Niedostępna"

        val powerManager = appContext.getSystemService(PowerManager::class.java)
        val thermalLabel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            powerManager?.currentThermalStatus?.let(::thermalStatusLabel) ?: "Niedostępny"
        } else {
            "Nieobsługiwany przez Android"
        }

        val displayManager = appContext.getSystemService(DisplayManager::class.java)
        val refreshRate = displayManager?.getDisplay(android.view.Display.DEFAULT_DISPLAY)
            ?.mode?.refreshRate
            ?.takeIf { it > 0f }
            ?.let { String.format(Locale.getDefault(), "%.0f Hz", it) }
            ?: "Niedostępna"

        val totalRam = memoryInfo.totalMem.takeIf { it > 0L }
            ?.let(::formatGigabytes) ?: "Niedostępna"
        val availableRam = memoryInfo.availMem.takeIf { it > 0L }
            ?.let(::formatGigabytes) ?: "Niedostępna"

        val lines = listOf(
            "Producent" to Build.MANUFACTURER.orEmpty().ifBlank { "Nieznany" },
            "Model" to Build.MODEL.orEmpty().ifBlank { "Nieznany" },
            "Android" to "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            "Architektury CPU" to Build.SUPPORTED_ABIS.take(3).joinToString(", ")
                .ifBlank { "Niedostępne" },
            "RAM łącznie" to totalRam,
            "RAM dostępna" to availableRam,
            "Niski poziom pamięci" to if (memoryInfo.lowMemory) "Tak" else "Nie",
            "Bateria" to (batteryLevel?.let { "$it%" } ?: "Niedostępna"),
            "Zasilanie" to chargingLabel,
            "Temperatura baterii" to batteryTemperature,
            "Stan termiczny Androida" to thermalLabel,
            "Odświeżanie ekranu" to refreshRate
        )

        return DeviceDiagnosticsReport(
            lines = lines,
            capturedAt = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            ).format(Date())
        )
    }

    private fun formatGigabytes(bytes: Long): String =
        String.format(Locale.getDefault(), "%.1f GB", bytes / 1024.0 / 1024.0 / 1024.0)

    private fun thermalStatusLabel(status: Int): String = when (status) {
        PowerManager.THERMAL_STATUS_NONE -> "Normalny"
        PowerManager.THERMAL_STATUS_LIGHT -> "Niewielkie obciążenie cieplne"
        PowerManager.THERMAL_STATUS_MODERATE -> "Umiarkowane"
        PowerManager.THERMAL_STATUS_SEVERE -> "Wysokie"
        PowerManager.THERMAL_STATUS_CRITICAL -> "Krytyczne"
        PowerManager.THERMAL_STATUS_EMERGENCY -> "Awaryjne"
        PowerManager.THERMAL_STATUS_SHUTDOWN -> "Wyłączenie termiczne"
        else -> "Nieznany"
    }
}
