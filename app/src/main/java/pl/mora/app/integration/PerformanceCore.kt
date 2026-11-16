package pl.mora.app.integration

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager

enum class PerformanceProfile(val title: String) {
    BALANCED("Zrównoważony"),
    BATTERY_SAVER("Oszczędzanie energii"),
    LOW_MEMORY("Oszczędzanie pamięci"),
    THERMAL_PROTECTION("Ochrona termiczna")
}

data class PerformanceAssessment(
    val profile: PerformanceProfile,
    val explanation: String,
    val reducedMotionRecommended: Boolean,
    val batteryPercent: Int?,
    val powerSaveMode: Boolean,
    val availableRamMb: Long?,
    val lowMemory: Boolean,
    val thermalStatus: Int?
)

/**
 * Produces a conservative recommendation from public Android signals.
 * It does not change CPU/GPU clocks, kill apps, or alter system power modes.
 */
object PerformanceCore {

    fun assess(context: Context): PerformanceAssessment {
        val appContext = context.applicationContext
        val powerManager = appContext.getSystemService(PowerManager::class.java)
        val activityManager = appContext.getSystemService(ActivityManager::class.java)
        val memory = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memory)

        val battery = appContext.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPercent = if (level >= 0 && scale > 0) {
            (level * 100f / scale).toInt()
        } else null

        val powerSave = powerManager?.isPowerSaveMode == true
        val thermal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            powerManager?.currentThermalStatus
        } else null
        val lowMemory = memory.lowMemory
        val availableRamMb = memory.availMem.takeIf { it > 0L }?.div(1024L * 1024L)

        val profile = when {
            thermal != null && thermal >= PowerManager.THERMAL_STATUS_SEVERE ->
                PerformanceProfile.THERMAL_PROTECTION
            lowMemory || (availableRamMb != null && availableRamMb < 700L) ->
                PerformanceProfile.LOW_MEMORY
            powerSave || (batteryPercent != null && batteryPercent <= 20) ->
                PerformanceProfile.BATTERY_SAVER
            else -> PerformanceProfile.BALANCED
        }

        val explanation = when (profile) {
            PerformanceProfile.THERMAL_PROTECTION ->
                "Android zgłasza wysoki stan termiczny. Ogranicz animacje i przerwij ciężkie zadania Mory."
            PerformanceProfile.LOW_MEMORY ->
                "Dostępna pamięć jest ograniczona. Zalecane są prostsze animacje i krótsze zadania w tle."
            PerformanceProfile.BATTERY_SAVER ->
                "Aktywny jest tryb oszczędzania energii lub poziom baterii jest niski."
            PerformanceProfile.BALANCED ->
                "Nie wykryto sygnałów wymagających ograniczenia efektów Mory."
        }

        return PerformanceAssessment(
            profile = profile,
            explanation = explanation,
            reducedMotionRecommended = profile != PerformanceProfile.BALANCED,
            batteryPercent = batteryPercent,
            powerSaveMode = powerSave,
            availableRamMb = availableRamMb,
            lowMemory = lowMemory,
            thermalStatus = thermal
        )
    }
}
