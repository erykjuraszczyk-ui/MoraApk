package pl.mora.app.integration

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

/**
 * Optional, user-initiated discovery and launch of the factory Mora app.
 *
 * Discovery is limited to launcher activities visible through the manifest
 * <queries> declaration. No private REDMAGIC API or package name is assumed.
 */
data class CompanionApp(
    val packageName: String,
    val displayName: String
)

sealed interface CompanionLaunchResult {
    data object Launched : CompanionLaunchResult
    data object NotInstalledOrNotVisible : CompanionLaunchResult
    data object NoLaunchActivity : CompanionLaunchResult
    data class Failed(val reason: String) : CompanionLaunchResult
}

class CompanionAppDiscovery(context: Context) {
    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager

    fun findMoraApps(): List<CompanionApp> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val activities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                launcherIntent,
                PackageManager.ResolveInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(launcherIntent, 0)
        }

        return activities.mapNotNull { resolveInfo ->
            val label = resolveInfo.loadLabel(packageManager)?.toString()?.trim().orEmpty()
            val packageName = resolveInfo.activityInfo?.packageName ?: return@mapNotNull null
            if (label.contains("mora", ignoreCase = true)) {
                CompanionApp(packageName = packageName, displayName = label)
            } else {
                null
            }
        }.distinctBy { it.packageName }
            .sortedBy { it.displayName.lowercase() }
    }

    fun launch(packageName: String): CompanionLaunchResult {
        if (packageName.isBlank()) return CompanionLaunchResult.NotInstalledOrNotVisible

        val launchIntent = try {
            packageManager.getLaunchIntentForPackage(packageName)
        } catch (error: SecurityException) {
            return CompanionLaunchResult.Failed(
                error.message ?: "Brak uprawnień do uruchomienia aplikacji."
            )
        } ?: return CompanionLaunchResult.NoLaunchActivity

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            appContext.startActivity(launchIntent)
            CompanionLaunchResult.Launched
        } catch (_: ActivityNotFoundException) {
            CompanionLaunchResult.NoLaunchActivity
        } catch (error: SecurityException) {
            CompanionLaunchResult.Failed(
                error.message ?: "System odmówił uruchomienia aplikacji."
            )
        }
    }
}
