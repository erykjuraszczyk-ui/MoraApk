package pl.mora.app.update

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

/**
 * Read-only package diagnostics for the companion-app update flow.
 *
 * This does not modify, launch, patch, or uninstall another application.
 */
data class InstalledAppIdentity(
    val packageName: String,
    val versionName: String?,
    val versionCode: Long,
    val signingCertificateSha256: List<String>
)

object UpdateCompatibility {

    fun inspectInstalledPackage(
        context: Context,
        packageName: String
    ): InstalledAppIdentity? {
        val packageManager = context.packageManager

        val packageInfo = try {
            getPackageInfo(packageManager, packageName)
        } catch (_: PackageManager.NameNotFoundException) {
            return null
        }

        return InstalledAppIdentity(
            packageName = packageInfo.packageName,
            versionName = packageInfo.versionName,
            versionCode = packageInfo.longVersionCode,
            signingCertificateSha256 = getCertificateDigests(packageInfo)
        )
    }

    fun isSupportedAndroid(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    @Suppress("DEPRECATION")
    private fun getPackageInfo(
        packageManager: PackageManager,
        packageName: String
    ): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(
                    PackageManager.GET_SIGNING_CERTIFICATES.toLong()
                )
            )
        } else {
            packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
        }
    }

    private fun getCertificateDigests(
        packageInfo: PackageInfo
    ): List<String> {
        val signingInfo = packageInfo.signingInfo ?: return emptyList()
        val certificates = if (signingInfo.hasMultipleSigners()) {
            signingInfo.apkContentsSigners
        } else {
            signingInfo.signingCertificateHistory
        }

        return certificates
            .map { signature ->
                val digest = MessageDigest.getInstance("SHA-256")
                    .digest(signature.toByteArray())
                digest.joinToString("") { byte -> "%02x".format(byte) }
            }
            .distinct()
    }
}
